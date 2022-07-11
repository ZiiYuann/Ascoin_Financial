package com.tianli.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.charge.ChargeType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.currency.enums.CurrencyAdaptType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
public class ChargeVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 交易类型
     */
    private ChargeType chargeType;

    /**
     * 交易状态
     */
    private ChargeStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 交易hash
     */
    private String txid;

    /**
     * 发送地址
     */
    private String fromAddress;

    /**
     * 接受地址
     */
    private String toAddress;

    /**
     * 金额
     */
    private BigDecimal realAmount;

    /**
     * 币种类型
     */
    private CurrencyAdaptType currencyAdaptType;

}
