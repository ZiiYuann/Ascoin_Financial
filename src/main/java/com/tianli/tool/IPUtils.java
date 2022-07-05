package com.tianli.tool;

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

import java.util.Map;
import java.util.Objects;

@Slf4j
public class IPUtils {

    /**
     {
         organization: "China Telecom",
         longitude: 120.1612,
         city: "Hangzhou",
         timezone: "Asia/Shanghai",
         isp: "China Telecom",
         offset: 28800,
         region: "Zhejiang",
         asn: 4134,
         asn_organization: "Chinanet",
         country: "China",
         ip: "115.205.65.73",
         latitude: 30.2994,
         continent_code: "AS",
         country_code: "CN",
         region_code: "ZJ"
     }
     */
    private static final RateLimiter LIMIT = RateLimiter.create(5);

    public static Map<String, String> ipAnalysis(String ip) throws Exception {
        double acquire = LIMIT.acquire();
        if(StringUtils.isBlank(ip)){
            return Map.of();
        }
        Map<String, String> query = Maps.newHashMap();
        query.put("ip", ip);
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBY2NvdW50SWQiOiJlZmQzNDY3MWFmNjRjNTg1ZTJhMWUxMjEyNWQ5OWQzNiJ9.DW-2GfWa8PD8-k6UhLEL-joe298toT8s1yoCWjlCisg";
        ConfigService configService = ApplicationContextTool.getBean("configService", ConfigService.class);
        if(Objects.nonNull(configService)){
            String ip_analysis_token = configService._get("ip_analysis_token");
            if(StringUtils.isNotBlank(ip_analysis_token)){
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
    static class IpAnalysisInfo{
        private Integer code;
        private String message;
        private Map<String, String> data;
    }
}
