package com.tianli.agent.management.vo;

import com.tianli.product.financial.enums.ProductType;
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

    private String coin;

    private ProductType type;

    private BigDecimal rate;

    private BigDecimal holdAmount;

    private Integer holdCount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitPayInterestAmount;

}
