package com.tianli.management.fundmanagement;

import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalManagePO {
    private Long id;
    private LocalDateTime create_time;
    private String uid_username;
    private String uid_nick;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private CurrencyTokenEnum token;
    private TokenCurrencyType currency_type;
    private BigInteger amount;
    private BigInteger fee;
    private BigInteger real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String note;

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

    private String user_type;
    /**
     * 审核人
     */
    private String reviewer;
    /**
     * 审核时间
     */
    private LocalDateTime reviewer_time;


    private String reason;

    private String reason_en;

    private String review_note;
}
