package com.tianli.financial.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-29
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FinancialProductRateVO extends FinancialProductVO{

    /**
     * 最大利率
     * @since 1.1
     */
    private BigDecimal maxRate;

    /**
     * 最小利率
     */
    private BigDecimal minRate;

    /**
     * 产品列表
     */
    private List<FinancialProductVO> products;
}
