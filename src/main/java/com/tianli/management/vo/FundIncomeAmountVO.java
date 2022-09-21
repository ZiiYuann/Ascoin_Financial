package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundIncomeAmountVO {

    private BigDecimal interestAmount;
}
