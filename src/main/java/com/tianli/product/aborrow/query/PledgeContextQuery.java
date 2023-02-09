package com.tianli.product.aborrow.query;

import com.tianli.product.aborrow.enums.PledgeType;
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
public class PledgeContextQuery {

    @NotNull
    private PledgeType pledgeType;

    @NotNull
    private List<Long> recordIds;

    @NotNull
    private String coin;

    @NotNull
    private BigDecimal pledgeAmount;


}
