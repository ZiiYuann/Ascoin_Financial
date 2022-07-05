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
     * 更新时间
     */
    private LocalDateTime update_time;

    /**
     * 用户id, username
     */
    private Long uid;
    private String username;
    private String nick;

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
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 用户的行为
     */
    private GrcCheckModular behavior;

    /**
     * 操作关联的id
     *
     * 验证码登录/密码登录 -> 生成个随机的id
     * 领取新人福利 -> 领取log的id
     * 领取每日奖励 -> 领取的log的id
     * 下注 -> 下注记录的id
     * 提现 -> 提现记录的id
     * 邀请绑定 -> 邀请绑定记录的id
     */
    private Long behavior_id;

    private String method;

    /**
     * 谷歌校验分数
     */
    private Double grc_score;

    /**
     * 谷歌的人机校验结果
     *
     * 0: 失败
     * 1: 成功
     */
    private Boolean grc_result;

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
                .nick(risk.getNick())
                .ip(requestInit.getIp())
                .equipment_type(requestInit.getDeviceType())
                .equipment(requestInit.getDeviceInfo())
                .behavior(modular)
                .behavior_id(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .grc_score(risk.getGrcScore())
                .grc_result(risk.isGrc())
                .method(risk.getRooMethodName())
                .state(0)
                .build();
        userIpLogService.save(saveLog);
        return saveLog;
    }
}
