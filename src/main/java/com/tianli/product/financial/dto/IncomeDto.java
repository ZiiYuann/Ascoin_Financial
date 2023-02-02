package com.tianli.product.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeDto {

    private String coin;

    private BigDecimal holdAmount;

    /**
     * 累计收益
     */
    private BigDecimal accrueIncomeAmount;

    /**
     * 每日收益
     */
    private BigDecimal dailyIncomeAmount;

    /**
     * 已经计算收益
     */
    private BigDecimal calIncomeAmount;

    /**
     * 待审核收益
     */
    private BigDecimal waitIncomeAmount;


}
