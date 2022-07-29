package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowRepayPageVO {

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 待还余额
     */
    private BigDecimal totalRepayAmount;

    /**
     * 本金还款
     */
    private BigDecimal repayCapital;

    /**
     * 利息还款
     */
    private BigDecimal repayInterest;

    /**
     * 质押率
     */
    private BigDecimal pledgeRate;

    /**
     * 释放质押数额
     */
    private BigDecimal releasePledgeAmount;

}
