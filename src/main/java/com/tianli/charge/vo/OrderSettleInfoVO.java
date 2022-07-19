package com.tianli.charge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
public class OrderSettleInfoVO {

    private Long uid;

    /**
     * financial_record id
     */
    private Long recordId;

    private String coin;

    private String productName;

    private String productTerm;

    /**
     * 累计收益
     */
    private BigDecimal incomeAmount;

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
    private LocalDateTime startIncomeTime;

    /**
     * 赎回时间
     */
    private LocalDateTime redeemTime;


}
