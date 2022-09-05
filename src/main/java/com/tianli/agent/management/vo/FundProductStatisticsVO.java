package com.tianli.agent.management.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundProductStatisticsVO {

    private Long productId;

    private String productName;

    private CurrencyCoin coin;

    private ProductType type;

    private BigDecimal rate;

    private BigDecimal holdAmount;

    private Integer holdCount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitPayInterestAmount;



}
