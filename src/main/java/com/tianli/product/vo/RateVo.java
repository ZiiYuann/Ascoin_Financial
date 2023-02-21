package com.tianli.product.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateVo {

    private String coin;

    private BigDecimal usdtRate;

    private BigDecimal usdtCnyRate;
}
