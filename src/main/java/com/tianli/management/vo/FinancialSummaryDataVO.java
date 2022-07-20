package com.tianli.management.vo;

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
@AllArgsConstructor
@NoArgsConstructor
public class FinancialSummaryDataVO {

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
     * 累计收益
     */
    private BigDecimal incomeAmount;
}
