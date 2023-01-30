package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MUserListVO extends FinancialUserInfoVO {


    /**
     * 已经计算收益
     */
    private BigDecimal calIncomeAmount = BigDecimal.ZERO;

    /**
     * 待发利息
     */
    private BigDecimal waitIncomeAmount = BigDecimal.ZERO;
}
