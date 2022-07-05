package com.tianli.management.logs;

import com.tianli.common.Constants;
import com.tianli.user.logs.mapper.UserIpLog;
import com.tianli.user.userinfo.mapper.UserInfo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@Data
public class UserLoginVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 注册, 登录时间
     */
    private LocalDateTime reg_time;
    private LocalDateTime login_time;

    /**
     * 用户id, username
     */
    private String username;
    private String nick;

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
     * 登录的ip
     */
    private String ip;

    /**
     * 设备信息
     * 类型和型号
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

    public static UserLoginVO convert(UserIpLog e, UserInfo user) {
        return UserLoginVO.builder()
                .id(e.getId())
                .reg_time(Objects.isNull(user) ? e.getCreate_time() : user.getCreate_time())
                .login_time(e.getCreate_time())
                .username(e.getUsername())
                .nick(StringUtils.isBlank(e.getNick()) ? Constants.defaultUserNick : e.getNick())
                .grc_score(e.getGrc_score())
                .grc_result(e.getGrc_result())
                .ip(e.getIp())
                .equipment_type(e.getEquipment_type())
                .equipment(e.getEquipment())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .country(e.getCountry())
                .region(e.getRegion())
                .city(e.getCity())
                .build();
    }
}
