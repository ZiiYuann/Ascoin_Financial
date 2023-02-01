package com.tianli.product.fund.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundTransactionAmountDTO {

    private String coin;

    private BigDecimal purchaseAmount;

    private BigDecimal redemptionAmount;

}
