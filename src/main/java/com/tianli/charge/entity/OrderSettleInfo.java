package com.tianli.charge.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
public class OrderSettleInfo {

    private Long uid;

    /**
     * financial_record id
     */
    private Long recordId;

    /**
     * 累计收益
     */
    private BigDecimal income;

    /**
     * 结算数额
     */
    private BigDecimal settleAmount;

    /**
     * 申购时间
     */
    private LocalDateTime purchaseTime;

    /**
     * 记息时间
     */
    private LocalDateTime startTime;

    /**
     * 赎回时间
     */
    private LocalDateTime redeemTime;


}
