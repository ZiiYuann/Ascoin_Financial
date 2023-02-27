package com.tianli.product.aborrow.query;

import com.tianli.product.aborrow.enums.PledgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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

    private Long recordId;

    private String coin;

    private BigDecimal pledgeAmount;

}
