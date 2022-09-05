package com.tianli.fund.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundMainPageVO {

    /**
     * 持有金额
     */
    @BigDecimalFormat("#0.00")
    private BigDecimal holdAmount;

    /**
     * 已发利息
     */
    @BigDecimalFormat("#0.00")
    private BigDecimal payInterestAmount;

    /**
     * 待发利息
     */
    @BigDecimalFormat("#0.00")
    private BigDecimal waitPayInterestAmount;
}
