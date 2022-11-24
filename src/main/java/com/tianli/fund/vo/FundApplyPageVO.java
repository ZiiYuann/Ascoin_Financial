package com.tianli.fund.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FundApplyPageVO {
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 产品名称
     */
    private String productName;

    /**
     * 英文名称
     */
    private String productNameEn;

    /**
     * 币种
     */
    private String coin;

    /**
     * logo
     */
    private String logo;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 可用金额
     */
    private BigDecimal availableAmount;

    /**
     * 预计收益
     */
    private BigDecimal expectedIncome;

    /**
     * 个人额度
     */
    private BigDecimal personQuota;

    private BigDecimal personHoldAmount;

    /**
     * 总额度
     */
    private BigDecimal totalQuota;

    private BigDecimal totalHoldAmount;

    /**
     * 申购时间
     */
    private LocalDate purchaseTime;

    /**
     * 利息计算时间
     */
    private LocalDate interestCalculationTime;

    /**
     * 收益发放时间
     */
    private LocalDate incomeDistributionTime;

    /**
     * 赎回周期
     */
    private Long redemptionCycle;

    /**
     * 到账时间
     */
    private Long accountDate;

    /**
     * 赎回时间
     */
    private LocalDate redemptionTime;


}
