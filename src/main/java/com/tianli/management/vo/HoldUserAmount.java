package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoldUserAmount {

    private BigDecimal holdAmount;

    private BigDecimal interestAmount;

    private BigDecimal waitInterestAmount;

    private BigDecimal payInterestAmount;

}
