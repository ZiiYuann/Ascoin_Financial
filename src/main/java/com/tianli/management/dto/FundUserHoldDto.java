package com.tianli.management.dto;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundUserHoldDto {

    private CurrencyCoin coin;

    private BigDecimal holdAmount;

    private BigDecimal interestAmount;

    private BigDecimal waitInterestAmount;

    private BigDecimal payInterestAmount;
}
