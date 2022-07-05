package com.tianli.management.loan.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/5/26 18:39
 */
@Data
public class LoanAuditDetailsVo {

    private Long id;

    /**
     * 期望借款金额
     */
    private BigDecimal expect_amount;

    /**
     * 借款的币种
     */
    private String token;

    /**
     * 日利率
     */
    private BigDecimal rate;

    /**
     * 还款周期
     */
    private Integer repayment_cycle;

    /**
     * 预期利息
     */
    private BigDecimal expected_interest;

    /**
     * 可用总余额
     */
    private BigDecimal total_amount;
    /**
     * 需还款金额
     */
    private BigDecimal repayment_required;

    private String image_1;

    private String image_2;

    private String image_3;

    private String image_4;
}
