package com.tianli.fund.dto;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundIncomeAmountDTO {

    private BigDecimal totalAmount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitInterestAmount;

    private CurrencyCoin coin;

}
