package com.tianli.management.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundUserHoldDto {

    private String coin;

    private BigDecimal holdAmount;

    private BigDecimal interestAmount;

    private BigDecimal waitInterestAmount;

    private BigDecimal payInterestAmount;
}
