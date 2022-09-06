package com.tianli.agent.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldDataVO {
    private BigDecimal holdAmount;

    private Integer holdCount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitPayInterestAmount;
}
