package com.tianli.product.aborrow.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalPledgeQuery {

    @NotNull
    private List<PledgeContextQuery> pledgeContext;

    @NotNull
    private String coin;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Boolean borrow;

    public BigDecimal getAmount() {
        if (borrow) {
            return amount;
        }
        return amount.multiply(new BigDecimal(-1L));
    }
}
