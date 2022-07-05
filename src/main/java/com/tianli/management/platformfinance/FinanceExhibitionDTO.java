package com.tianli.management.platformfinance;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-01-12 16:42
 * @since 1.0.0
 */
@Data
public class FinanceExhibitionDTO {
    private LocalDate date;
    private BigInteger platform_profit;
    private BigInteger rebate_amount;
    private BigInteger agent_dividends;
    private BigInteger settled_number;
    private BigInteger userFeeErc20;
    private BigInteger userFeeOmni;
    private BigInteger depositFeeErc20;
    private BigInteger depositFeeOmni;
    private BigInteger settlementFeeErc20;
    private BigInteger settlementFeeOmni;
}
