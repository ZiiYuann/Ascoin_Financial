package com.tianli.management.currency;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.logs.mapper.UserIpLog;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户每日奖励记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class NewGiftRecordVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户账户
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nick;

    /**
     * 创建时间
     */
    private String create_time;

    /**
     * 余额类型
     */
    private CurrencyTokenEnum token;

    /**
     * 数额
     */
    private Double amount;


    /* 新加的IP相关信息 */

    /**
     * 谷歌校验分数
     */
    private Double grc_score;
    private Boolean grc_result;

    /**
     * 设备信息
     */
    private String ip;
    private String equipment_type;
    private String equipment;
    private Long behavior_id;

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

    public static NewGiftRecordVO convert(UserIpLog log){
        NewGiftRecordVO build = NewGiftRecordVO.builder()
                .id(log.getId())
                .username(log.getUsername())
                .create_time(TimeTool.getDateTimeDisplayString(log.getCreate_time()))
                .build();

        build.grc_score = log.getGrc_score();
        build.grc_result = log.getGrc_result();
        build.ip = log.getIp();
        build.equipment_type = log.getEquipment_type();
        build.behavior_id = log.getBehavior_id();
        build.equipment = log.getEquipment();
        build.country = log.getCountry();
        build.region = log.getRegion();
        build.city = log.getCity();
        return build;

    }
}
