package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
@Builder
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
     * 定期宝数额
     */
    private BigDecimal currentAmount;

    /**
     * 活期宝数额
     */
    private BigDecimal fixedAmount;

    /**
     * 累计盈亏
     */
    private BigDecimal profitAndLossAmount;
}
