package com.tianli.sso.init;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author wangqiyun
 * @Date 2019/3/15 5:23 PM
 */
@Data
public class RequestInit {
    private LocalDateTime now = LocalDateTime.now();
    private Long now_ms = System.currentTimeMillis();
    private Long uid;
    private String address;
    private SignUserInfo userInfo;
    private String imei = "";
    private String deviceType = "";
    /**
     * 设备信息
     */
    private String deviceInfo = "";
    private String requestId = UUID.randomUUID().toString();
    private String ip = "";
    private Double lat;
    private Double lng;
    private String token;
}
