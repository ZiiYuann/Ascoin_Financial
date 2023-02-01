package com.tianli.product.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 产品rate Dto
 * @author chenb
 * @apiNote
 * @since 2022-08-29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRateDTO {

    private String coin;

    private Integer productCount;

    private BigDecimal maxRate;

    private BigDecimal minRate;

    private Long id;
}
