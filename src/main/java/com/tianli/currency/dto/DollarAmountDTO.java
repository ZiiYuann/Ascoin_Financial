package com.tianli.currency.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
public class DollarAmountDTO {

    /**
     * 美元汇率
     */
    private BigDecimal rate;

    /**
     * 原始金额
     */
    private BigDecimal originalAmount;

    /**
     * 美元金额
     */
    private BigDecimal dollarAmount;
}
