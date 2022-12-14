package com.tianli.tool;

import com.google.gson.Gson;
import com.tianli.common.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class IPUtils {


    public static Map<String, String> ipAnalysis(String ip) throws Exception {
        HttpResponse get = HttpUtils.doGet("https://api.ip.sb/geoip/" + ip, "", "", Map.of(), Map.of());
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
            e.printStackTrace();
        }
        return ip;
    }


}
