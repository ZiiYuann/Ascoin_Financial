package com.tianli.financial.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProductLadderRate {

    private Long id;

    private Long productId;

    private BigDecimal startPoint;

    private BigDecimal endPoint;

    private BigDecimal rate;
}
