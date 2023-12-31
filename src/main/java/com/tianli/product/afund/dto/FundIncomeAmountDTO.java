package com.tianli.product.afund.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundIncomeAmountDTO {

    private BigDecimal totalAmount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitInterestAmount;

    private String coin;

}
