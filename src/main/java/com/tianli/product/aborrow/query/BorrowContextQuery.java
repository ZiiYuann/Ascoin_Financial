package com.tianli.product.aborrow.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowContextQuery {

    private String coin;

    private BigDecimal amount;
}
