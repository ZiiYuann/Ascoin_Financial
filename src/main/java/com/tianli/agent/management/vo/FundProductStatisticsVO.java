package com.tianli.agent.management.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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