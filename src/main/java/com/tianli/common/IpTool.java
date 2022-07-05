package com.tianli.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author wangqiyun
 * @Date 2019/2/26 5:22 PM
 */
@Slf4j
@Service
public class IpTool {
    public String getIp() {
        return this.getIp(httpServletRequest);
    }

    public String getIp(HttpServletRequest request) {//获得客户端的IP,如果有更好的方法可以直接代替
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }

        return ipAddress;
    }

    public Map<String, Object> analysisIpPosition(String ip) {
        // 阿里云服务地址: https://market.aliyun.com/products/57002003/cmapi021970.html?spm=5176.2020520132.101.3.fb647218aKEhOb#sku=yuncode15970000018
        String host = "https://api01.aliyun.venuscn.com";
        String path = "/ip";
        String method = "GET";
        String appCodeData = configService._get("ali_ip_analysis_app_code");
        String appCode = StringUtils.isBlank(appCodeData) ?  "25db932466cf4873aeb9e10b18347233" : appCodeData;
        Map<String, String> headers = new HashMap<>(2);
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<>(2);
        querys.put("ip", ip);
        Map<String, Object> addressInfo = new HashMap<>();
        try {
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            String dataJsonString = EntityUtils.toString(response.getEntity());
            /*
            {
            "data":
                {
                    "ip":"218.18.228.178",
                    "long_ip":"3658671282",
                    "isp":"电信",
                    "area":"华南",
                    "region_id":"440000",
                    "region":"广东",
                    "city_id":"440300",
                    "city":"深圳",
                    "country_id":"CN",
                    "country":"中国"
                },
                "ret":200,
                "msg":"success",
                "log_id":"a80a8141f29c40aea3a2fce81e8a6a7e"
            }
            */

            JsonObject jsonObject = new Gson().fromJson(dataJsonString, JsonObject.class);
            JsonElement data;
            if(jsonObject != null && (data = jsonObject.get("data")) != null){
                JsonObject dataJsonObject = data.getAsJsonObject();
                JsonObject retData = new JsonObject();
                // 国家 省 市
                String country = dataJsonObject.get("country").getAsString();
                String region = dataJsonObject.get("region").getAsString();
                String city = dataJsonObject.get("city").getAsString();
                retData.addProperty("country", country);
                retData.addProperty("region", region);
                retData.addProperty("city", city);
                // 其他信息
                String isp = dataJsonObject.get("isp").getAsString();
                String area = dataJsonObject.get("area").getAsString();
                retData.addProperty("area", area);
                retData.addProperty("isp", isp);
                addressInfo.put("code", 0);
                addressInfo.put("data", retData);
            }else{
                addressInfo.put("code", -2);
                addressInfo.put("errMsg", "解析ip信息为空");
            }
        } catch (Exception e) {
            addressInfo.put("code", -1);
            addressInfo.put("errMsg", e.getMessage());
            log.warn("IpTool:analysisIpPosition异常!!!\n获取ip:[{}]异常, 异常信息:[{}]", ip, e.getMessage());
        }
        return addressInfo;
    }

    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private ConfigService configService;
    private static final Pattern IP_PATTERN = Pattern.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})$");
}
