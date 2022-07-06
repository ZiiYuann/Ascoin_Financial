package com.tianli.user.logs.mapper;

import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInit;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.RequestRiskManagementInfo;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.user.logs.UserIpLogService;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@Data
public class UserIpLog {
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 用户id, username
     */
    private Long uid;
    private String username;

    /**
     * 登录的ip
     */
    private String ip;

    /**
     * 设备信息
     */
    private String equipment_type;
    private String equipment;

    /**
     * 国家
     */
    private String country;

    /**
     * 地区
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * ip解析的状态, 0:未解析 1:已解析
     */
    private Integer state;

    public static UserIpLog save(GrcCheckModular modular,
                                 RequestInitService requestInitService,
                                 UserIpLogService userIpLogService) {
        RequestInit requestInit = requestInitService.get();
        RequestRiskManagementInfo risk = requestInitService.getRisk();
        LocalDateTime now = LocalDateTime.now();
        UserIpLog saveLog = UserIpLog.builder()
                .id(CommonFunction.generalId())
                .create_time(now)
                .uid(requestInit.getUid())
                .username(risk.getUsername())
                .ip(requestInit.getIp())
                .equipment_type(requestInit.getDeviceType())
                .equipment(requestInit.getDeviceInfo())
                .state(0)
                .build();
        userIpLogService.save(saveLog);
        return saveLog;
    }
}
