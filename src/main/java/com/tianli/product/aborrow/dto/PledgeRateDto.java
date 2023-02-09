package com.tianli.product.aborrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class PledgeRateDto {

    private BigDecimal lqPledgeRate;

    // 质押总金额
    private BigDecimal pledgeAmount;

    // 强平可借
    private BigDecimal LqBorrowAmount;

}
