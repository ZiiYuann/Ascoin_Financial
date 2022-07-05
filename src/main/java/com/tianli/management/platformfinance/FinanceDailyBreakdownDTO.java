package com.tianli.management.platformfinance;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-02-20 13:49
 * @since 1.0.0
 */
@Data
public class FinanceDailyBreakdownDTO {
    private LocalDate date;
    private BigInteger sum_platform_profit;
    private BigInteger sum_rake;
    private BigInteger fee_erc20;
    private BigInteger fee_omni;
    private BigInteger charge_miner_eth_fee;
    private BigInteger charge_miner_btc_fee;
    private BigInteger sum_rebate;
    private BigInteger interest;
    private BigInteger agent_rake;
    private BigInteger miner_eth_fee;
    private BigInteger miner_btc_fee;

}
