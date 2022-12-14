package com.tianli.tool;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
public class IPUtils {


    public static JSONObject ipAnalysis(String ip) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://api.ip.sb/geoip/" + ip);
        HttpResponse response = client.execute(httpGet);
        String s = EntityUtils.toString(response.getEntity());
        return JSONUtil.parseObj(s);
    }

    @Data
    static class IpAnalysisInfo {
        private Integer code;
        private String message;
        private Map<String, String> data;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = "";
        try {
            // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && ip.length() > 0 && !"unKnown".equalsIgnoreCase(ip)) {
                // 多次反向代理后会有多个ip值，第一个ip才是真实ip
                int index = ip.indexOf(",");
                if (index != -1) {
                    return ip.substring(0, index);
                } else {
                    return ip;
                }
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && ip.length() > 0 && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }


}
