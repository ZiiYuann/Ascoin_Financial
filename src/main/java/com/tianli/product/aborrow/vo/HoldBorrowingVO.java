package com.tianli.product.aborrow.vo;

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
public class HoldBorrowingVO {

    private Long id;

    private String coin;

    private BigDecimal amount;

    private String logo;

    private BigDecimal hourRate;
}
