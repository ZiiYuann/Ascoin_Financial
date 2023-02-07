package com.tianli.product.aborrow.query;

import com.tianli.common.query.IoUQuery;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BorrowConfigCoinIoUQuery extends IoUQuery {

    @NotBlank
    private String coin;

    @NotBlank
    private String logo;

    @NotNull
    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    @NotNull
    private BigDecimal hourRate;

    @NotNull
    private Long weight;

}
