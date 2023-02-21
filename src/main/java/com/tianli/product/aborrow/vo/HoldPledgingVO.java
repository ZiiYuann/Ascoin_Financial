package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.enums.PledgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldPledgingVO {

    private Long id;

    private PledgeType pledgeType;

    private String coin;

    private BigDecimal amount;

    private String logo;

}
