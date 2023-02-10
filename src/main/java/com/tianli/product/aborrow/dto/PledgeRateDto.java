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

    // 质押总金额
    private BigDecimal pledgeAmount;

    // 强平可借
    private BigDecimal LqAmount;

    private BigDecimal lqPledgeRate;

    private BigDecimal warnAmount;

    private BigDecimal warnPledgeRate;

    private BigDecimal assureLqAmount;

    private BigDecimal assureLqPledgeRate;

    private BigDecimal borrowAmount;

    private BigDecimal currencyPledgeRate;


}
