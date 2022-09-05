package com.tianli.fund.dto;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundTransactionAmountDTO {

    private CurrencyCoin coin;

    private BigDecimal purchaseAmount;

    private BigDecimal redemptionAmount;

}
