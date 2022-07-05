package com.tianli.charge.mapper;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.ChargeType;
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
public class Charge {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;
    private String uid_username;
    private String uid_nick;
    private String uid_avatar;
    private String sn;
    private TokenCurrencyType currency_type;
    private ChargeType charge_type;
    private BigInteger amount;
    private BigInteger fee;
    private BigInteger real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String note;
    private String review_note;
    private BigInteger miner_fee;
    private TokenCurrencyType miner_fee_type;
    private CurrencyTokenEnum token;
    private String reviewer;

    private Long reviewer_id;

    private LocalDateTime reviewer_time;

    private String reason;

    private String reason_en;
}
