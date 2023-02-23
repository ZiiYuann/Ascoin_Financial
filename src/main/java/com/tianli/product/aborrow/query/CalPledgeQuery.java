package com.tianli.product.aborrow.query;

import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private List<PledgeContextQuery> pledgeContext;

    private List<BorrowContextQuery> borrowContext;

    private String coin;

    private BigDecimal amount = BigDecimal.ZERO;

    private Boolean borrow;

    private ModifyPledgeContextType pledgeContextType = ModifyPledgeContextType.ADD;

    public BigDecimal getAmount() {
        if (borrow) {
            return amount;
        }
        return amount.multiply(new BigDecimal(-1L));
    }
}
