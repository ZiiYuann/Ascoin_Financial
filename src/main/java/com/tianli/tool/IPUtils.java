package com.tianli.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.other.vo.IpInfoVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Slf4j
public class IPUtils {


    public static IpInfoVO ipAnalysis(String ip) throws Exception {
        return JSONUtil.toBean(HttpUtil.get("https://api.ip.sb/geoip/" + ip), IpInfoVO.class);
    }

    @Data
    static class IpAnalysisInfo {
        private Integer code;
        private String message;
        private Map<String, String> data;
    }

    public static String getIpAddress(HttpServletRequest request) {//获得客户端的IP,如果有更好的方法可以直接代替
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
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                        ipAddress = inet.getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
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


}
