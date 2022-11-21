package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-16
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAmountDetailsVO {

    /**
     * 账户余额
     */
    private BigDecimal dollarBalance;

    /**
     * 充值金额
     */
    private BigDecimal dollarRecharge;

    /**
     * 提币金额
     */
    private BigDecimal dollarWithdraw;

    /**
     * 理财金额
     */
    private BigDecimal dollarFinancial;

    /**
     * 基金金额
     */
    private BigDecimal dollarFund;

    /**
     * 理财金额利息
     */
    private BigDecimal dollarFinancialIncome;

    /**
     * 基金金额利息
     */
    private BigDecimal dollarFundIncome;

}
