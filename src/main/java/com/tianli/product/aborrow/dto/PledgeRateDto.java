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
    private BigDecimal pledgeFee = BigDecimal.ZERO;

    // 强平可借
    private BigDecimal LqFee = BigDecimal.ZERO;

    private BigDecimal lqPledgeRate = BigDecimal.ZERO;

    private BigDecimal warnFee = BigDecimal.ZERO;

    private BigDecimal warnPledgeRate = BigDecimal.ZERO;

    private BigDecimal assureLqFee = BigDecimal.ZERO;

    private BigDecimal assureLqPledgeRate = BigDecimal.ZERO;

    private BigDecimal borrowFee = BigDecimal.ZERO;

    private BigDecimal currencyPledgeRate = BigDecimal.ZERO;

    private BigDecimal interestFee = BigDecimal.ZERO;

    private BigDecimal initFee = BigDecimal.ZERO;


}
