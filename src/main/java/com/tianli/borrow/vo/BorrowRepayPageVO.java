package com.tianli.borrow.vo;

import com.tianli.common.annotation.BigDecimalFormat;
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
     * 待还总额
     */
    private BigDecimal waitRepayAmount;

    /**
     * 本次还款余额
     */
    private BigDecimal totalRepayAmount;

    /**
     * 本次本金还款
     */
    private BigDecimal repayCapital;

    /**
     * 本次利息还款
     */
    private BigDecimal repayInterest;

    /**
     * 质押率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal pledgeRate;

    /**
     * 释放质押数额
     */
    private BigDecimal releasePledgeAmount;


    private String coin;

    private String logo;

}
