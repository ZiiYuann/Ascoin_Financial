package com.tianli.financial.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpectIncomeVO {

    private BigDecimal expectIncome;
}
