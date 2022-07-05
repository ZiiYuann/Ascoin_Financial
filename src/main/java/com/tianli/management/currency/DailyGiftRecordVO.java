package com.tianli.management.currency;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.DailyGiftRecord;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.logs.mapper.UserIpLog;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * <p>
 * 用户每日奖励记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class DailyGiftRecordVO {

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
    private TokenCurrencyType token;

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

    public static DailyGiftRecordVO convert(DailyGiftRecord record){
        return DailyGiftRecordVO.builder()
                .id(record.getId())
                .username(record.getUsername())
                .nick(record.getNick())
                .create_time(TimeTool.getDateTimeDisplayString(record.getCreate_time()))
                .token(record.getToken())
                .amount(record.getToken().money(record.getAmount()))
                .build();

    }
    public static DailyGiftRecordVO convert(UserIpLog log){
        DailyGiftRecordVO giftRecordVO = DailyGiftRecordVO.builder()
                .id(log.getId())
                .username(log.getUsername())
                .nick(log.getNick())
                .create_time(TimeTool.getDateTimeDisplayString(log.getCreate_time()))
                .token(TokenCurrencyType.usdt_omni)
                .amount(0.0)
                .build();
        giftRecordVO.fillOtherProperties(log);
        return giftRecordVO;
    }

    public void fillOtherProperties(UserIpLog log){
        if(Objects.isNull(log)){
            return;
        }
        this.grc_score = log.getGrc_score();
        this.grc_result = log.getGrc_result();
        this.ip = log.getIp();
        this.equipment_type = log.getEquipment_type();
        this.equipment = log.getEquipment();
        this.behavior_id = log.getBehavior_id();
        this.country = log.getCountry();
        this.region = log.getRegion();
        this.city = log.getCity();
    }

}
