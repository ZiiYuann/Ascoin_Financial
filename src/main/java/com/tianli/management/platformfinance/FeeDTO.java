package com.tianli.management.platformfinance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-01-12 15:33
 * @since 1.0.0
 */
@Data
@Builder
public class FeeDTO {
    private LocalDate date;
    private BigDecimal charge_fee_erc20;
    private BigDecimal charge_fee_omni;
}
