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
 * @since 2023-02-09
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowCoinQuery {

    private List<PledgeContextQuery> pledgeContext;

    @NotNull
    private String borrowCoin;

    @NotNull
    private BigDecimal borrowAmount;

    @NotNull
    private Boolean autoReplenishment;

}
