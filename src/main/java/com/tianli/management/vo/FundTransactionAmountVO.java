package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundTransactionAmountVO {

    private BigDecimal purchaseAmount;

    private BigDecimal redemptionAmount;

}
