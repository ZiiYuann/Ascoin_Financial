package com.tianli.product.aborrow.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-16
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepayCoinQuery {

    @NotNull
    @DecimalMin(value = "0.00000001", message = "金额不能为0")
    private BigDecimal amount;

    @NotNull
    private String coin;
}
