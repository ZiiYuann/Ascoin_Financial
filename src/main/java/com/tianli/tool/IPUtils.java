package com.tianli.tool;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.tianli.common.HttpUtils;
import com.tianli.mconfig.ConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class IPUtils {

    /**
     * {
     * organization: "China Telecom",
     * longitude: 120.1612,
     * city: "Hangzhou",
     * timezone: "Asia/Shanghai",
     * isp: "China Telecom",
     * offset: 28800,
     * region: "Zhejiang",
     * asn: 4134,
     * asn_organization: "Chinanet",
     * country: "China",
     * ip: "115.205.65.73",
     * latitude: 30.2994,
     * continent_code: "AS",
     * country_code: "CN",
     * region_code: "ZJ"
     * }
     */
    private static final RateLimiter LIMIT = RateLimiter.create(5);

    public static Map<String, String> ipAnalysis(String ip) throws Exception {
        double acquire = LIMIT.acquire();
        if (StringUtils.isBlank(ip)) {
            return Map.of();
        }
        Map<String, String> query = Maps.newHashMap();
        query.put("ip", ip);
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBY2NvdW50SWQiOiJlZmQzNDY3MWFmNjRjNTg1ZTJhMWUxMjEyNWQ5OWQzNiJ9.DW-2GfWa8PD8-k6UhLEL-joe298toT8s1yoCWjlCisg";
        ConfigService configService = ApplicationContextTool.getBean("configService", ConfigService.class);
        if (Objects.nonNull(configService)) {
            String ip_analysis_token = configService._get("ip_analysis_token");
            if (StringUtils.isNotBlank(ip_analysis_token)) {
                token = ip_analysis_token;
            }
        }
        query.put("token", token);
        HttpResponse get = HttpUtils.doGet("https://www.douyacun.com", "/api/openapi/geo/location", "GET", Map.of(), query);
        String s = EntityUtils.toString(get.getEntity());
        IpAnalysisInfo result = new Gson().fromJson(s, IpAnalysisInfo.class);
        return Objects.isNull(result) ? Map.of() : result.getData();
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

        }
        return ip;
    }




}
