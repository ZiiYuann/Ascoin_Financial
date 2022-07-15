package com.tianli.charge.entity;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 如果订单为充值或者提现，此为链上交易信息表
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderChargeInfo{

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 金额 提币、充值等不包含手续费
     */
    private BigDecimal fee;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 真实的费用
     */
    private BigDecimal realFee;

    /**
     *
     * 矿工费
     */
    private BigDecimal minerFee;

    /**
     * 交易hash
     */
    private String txid;

    private String fromAddress;

    private String toAddress;

    private LocalDateTime createTime;

    /**
     * 币种包装类信息
     */
    private CurrencyAdaptType currencyAdaptType;

}
