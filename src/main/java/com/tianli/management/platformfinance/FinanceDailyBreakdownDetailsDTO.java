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
public class FinanceDailyBreakdownDetailsDTO {

    private LocalDate date;

    /**
     * 平台净盈亏
     */
    private BigInteger sum_platform_profit;

    /**
     * 押注时平台手续费
     */
    private BigInteger sum_rake;

    /**
     * 代理商分红净盈亏
     */
    private BigInteger agent_dividends;

    /**
     * 充值/提现 erc20的手续费
     */
    private BigInteger fee_erc20;

    /**
     * 充值/提现 omni的手续费
     */
    private BigInteger fee_omni;

    /**
     * 充值/提现 erc20的矿工费
     */
    private BigInteger charge_miner_eth_fee;

    /**
     * 充值/提现 omni的矿工费
     */
    private BigInteger charge_miner_btc_fee;

    /**
     * 总返佣
     */
    private BigInteger sum_rebate;

    /**
     * 已结算数额
     */
    private BigInteger settled_total;

    /**
     * eth链交易 矿工费
     */
    private BigInteger miner_eth_fee;

    /**
     * btc链交易 矿工费
     */
    private BigInteger miner_btc_fee;

}
