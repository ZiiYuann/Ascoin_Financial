package com.tianli.management.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialUserInfoVO {

    private Long uid;

    /**
     * 钱包余额
     */
    private BigDecimal balanceAmount;

    /**
     * 已经充值
     */
    private BigDecimal rechargeAmount;

    /**
     * 已经提币
     */
    private BigDecimal withdrawAmount;

    /**
     * 赚币amount
     */
    private BigDecimal moneyAmount;

    /**
     * 活期宝数额
     */
    private BigDecimal currentAmount;

    /**
     * 定期宝数额
     */
    private BigDecimal fixedAmount;

    /**
     * 总资产
     */
    @BigDecimalFormat("0.00")
    private BigDecimal assets;

    /**
     * 冻结余额 美元
     */
    @BigDecimalFormat("0.00")
    private BigDecimal freezeAmount;

    /**
     * 剩余余额 美元
     */
    @BigDecimalFormat("0.00")
    private BigDecimal remainAmount;

    /**
     * 基金持有
     */
    @BigDecimalFormat("0.00")
    private BigDecimal fundHoldAmount;

    /**
     * 借币金额
     */
    @BigDecimalFormat("0.00")
    private BigDecimal borrowAmount;

    /**
     * 基金累计盈亏
     */
    @BigDecimalFormat("0.00")
    private BigDecimal fundIncomeAmount;

    /**
     * 理财累计盈亏
     */
    @BigDecimalFormat("0.00")
    private BigDecimal financialIncomeAmount;

    /**
     * 理财持有
     */
    @BigDecimalFormat("0.00")
    private  BigDecimal financialHoldAmount;
}
