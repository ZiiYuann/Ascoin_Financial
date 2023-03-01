package com.tianli.product.aborrow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBorrowVO {

    private String coin;

    private String logo;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal hourRate;

    private BigDecimal remain;

    private BigDecimal rate;

    private BigDecimal borrowAmount;

    private BigDecimal borrowProportion;

    private BigDecimal maxBorrowAmount;

    private boolean hold;

}
