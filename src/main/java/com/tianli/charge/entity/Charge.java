package com.tianli.charge.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.ChargeType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    /**
     * 交易类型
     */
    private ChargeType chargeType;

    /**
     * 交易状态
     */
    private ChargeStatus status;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;
    private String uidUsername;
    private String uidNick;
    private String uidAvatar;
    private String sn;
    private CurrencyAdaptType currencyAdaptType;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal realAmount;
    private String fromAddress;
    private String toAddress;
    private String txid;
    private String note;
    private String reviewNote;
    private BigDecimal minerFee;
    private CurrencyAdaptType minerFeeType;
    private CurrencyAdaptType token;
    private String reviewer;

    private Long reviewerId;

    private LocalDateTime reviewerTime;

    private String reason;

    private String reasonEn;
}
