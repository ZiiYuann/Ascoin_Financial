package com.tianli.charge.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOrderAdvanceQuery {

    private BigDecimal amount;

    private Long productId;

    private Long id;

    private String txid;
}
