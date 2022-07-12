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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    /**
     * 订单号
     */
    private String sn;

    /**
     * 交易类型
     */
    private ChargeType chargeType;

    /**
     * 交易状态
     */
    private ChargeStatus status;


    /**
     * 设计到的余额id
     */
    private Long accountBalanceId;

    /**
     * 币种包装
     */
    private CurrencyAdaptType currencyAdaptType;


    /**
     * 金额 提币、充值等不包含手续费
     */
    private BigDecimal fee;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 真实等金额 提币 = amount - serviceAmount，充值 = amount - serviceAmount
     */
    private BigDecimal realFee;

    /**
     *
     * 矿工费
     */
    private BigDecimal minerFee;

    private String txid;
    private String uidUsername;
    private String uidNick;
    private String uidAvatar;
    private String fromAddress;
    private String toAddress;
    private String note;

    private String reviewer;

    private String reviewNote;

    private Long reviewerId;

    private LocalDateTime reviewerTime;

    private String reason;

    private String reasonEn;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}
