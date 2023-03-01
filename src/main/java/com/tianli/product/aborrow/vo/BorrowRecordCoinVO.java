package com.tianli.product.aborrow.vo;

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
public class BorrowRecordCoinVO {

    private String coin;

    private String logo;

    private BigDecimal amount;

    private BigDecimal interestAmount;

    private BigDecimal minAmount;

    private BigDecimal remain;

}
