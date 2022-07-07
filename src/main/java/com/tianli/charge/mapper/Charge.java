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
 * @author  wangqiyun
 * @since  2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Charge {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private ChargeStatus status;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;
    private String uidUsername;
    private String uidNick;
    private String uidAvatar;
    private String sn;
    private TokenCurrencyType currencyType;
    private ChargeType chargeType;
    private BigInteger amount;
    private BigInteger fee;
    private BigInteger realAmount;
    private String fromAddress;
    private String toAddress;
    private String txid;
    private String note;
    private String reviewNote;
    private BigInteger minerFee;
    private TokenCurrencyType minerFeeType;
    private CurrencyTokenEnum token;
    private String reviewer;

    private Long reviewerId;

    private LocalDateTime reviewerTime;

    private String reason;

    private String reasonEn;
}
