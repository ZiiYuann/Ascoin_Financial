package com.tianli.management.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-05
 **/
@Data
public class ProductLadderRateVO {

    private Long id;

    private Long productId;

    private BigDecimal startPoint;

    private BigDecimal endPoint;

    private BigDecimal rate;
}
