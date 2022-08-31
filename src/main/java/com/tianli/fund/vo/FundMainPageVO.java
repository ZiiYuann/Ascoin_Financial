package com.tianli.fund.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundMainPageVO {

    /**
     * 持有金额
     */
    private BigDecimal holdAmount;

    /**
     * 已发利息
     */
    private BigDecimal payInterestAmount;

    /**
     * 待发利息
     */
    private BigDecimal waitPayInterestAmount;
}
