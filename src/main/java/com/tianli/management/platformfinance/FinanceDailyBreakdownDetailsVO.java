package com.tianli.management.platformfinance;

import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 *  2021-02-20 13:49
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDailyBreakdownDetailsVO {

    /**
     * 时间
     */
    private LocalDate date;
    /**
     * 平台净盈亏
     */
    private Double sum_platform_profit;
    /**
     * 总手续费数额
     */
    private Double fee;
    /**
     * 代理商分红净盈亏
     */
    private Double agent_dividends;

    /**
     * 代理商已结算数额
     */
    private Double agentSettledTotal;

    /**
     * 总返佣
     */
    private Double sum_rebate;

    /**
     * 毛利
     */
    private Double gross_profit;

    /**
     * 归集成本
     */
    private Double miner_fee;

    public static FinanceDailyBreakdownDetailsVO trans(FinanceDailyBreakdownDetailsDTO dto){
        BigInteger fee = dto.getFee_omni().add(dto.getFee_erc20().multiply(new BigInteger("100")));

        BigInteger miner_btc_fee = dto.getCharge_miner_btc_fee().add(dto.getMiner_btc_fee());
        BigInteger miner_eth_fee = dto.getCharge_miner_eth_fee().add(dto.getMiner_eth_fee());

        DigitalCurrency miner_eth_fee_to_omni = new DigitalCurrency(TokenCurrencyType.eth, miner_eth_fee).toOther(TokenCurrencyType.usdt_omni);
        DigitalCurrency miner_btc_fee_to_omni = new DigitalCurrency(TokenCurrencyType.btc, miner_btc_fee).toOther(TokenCurrencyType.usdt_omni);
        BigInteger miner_fee_total = miner_btc_fee_to_omni.getAmount().add(miner_eth_fee_to_omni.getAmount());

        //毛利 总利润+手续费-返佣数额-归集成本
        BigInteger grossProfit = dto.getSum_platform_profit().add(fee).subtract(dto.getSum_rebate()).subtract(miner_fee_total);
        return FinanceDailyBreakdownDetailsVO.builder()
                .date(dto.getDate())
                .sum_platform_profit(TokenCurrencyType.usdt_omni.money(dto.getSum_platform_profit()))
                .fee(TokenCurrencyType.usdt_omni.money(fee))
                .miner_fee(TokenCurrencyType.usdt_omni.money(miner_fee_total))
                .sum_rebate(TokenCurrencyType.usdt_omni.money(dto.getSum_rebate()))
                .agent_dividends(TokenCurrencyType.usdt_omni.money(dto.getAgent_dividends()))
                .agentSettledTotal(TokenCurrencyType.usdt_omni.money(dto.getSettled_total()))
                .gross_profit(TokenCurrencyType.usdt_omni.money(grossProfit)).build();
    }



}
