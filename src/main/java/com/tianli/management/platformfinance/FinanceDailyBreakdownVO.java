package com.tianli.management.platformfinance;

import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-02-20 13:50
 * @since 1.0.0
 */
@Data
@Builder
public class FinanceDailyBreakdownVO {
    private LocalDate date;
    private Double sum_platform_profit;
    private Double sum_rake;
    private Double fee;
    private Double sum_rebate;
    private Double interest;
    private Double agent_rake;
    private Double miner_fee;
    private Double miner_btc_fee;
    private Double miner_eth_fee;
    private Double gross_profit;

    public static FinanceDailyBreakdownVO trans(FinanceDailyBreakdownDTO dto){
        BigInteger fee = dto.getFee_omni().add(dto.getFee_erc20().multiply(new BigInteger("100")));

        BigInteger miner_btc_fee = dto.getCharge_miner_btc_fee().add(dto.getMiner_btc_fee());
        BigInteger miner_eth_fee = dto.getCharge_miner_eth_fee().add(dto.getMiner_eth_fee());

        DigitalCurrency miner_eth_fee_to_omni = new DigitalCurrency(TokenCurrencyType.eth, miner_eth_fee).toOther(TokenCurrencyType.usdt_omni);
        DigitalCurrency miner_btc_fee_to_omni = new DigitalCurrency(TokenCurrencyType.btc, miner_btc_fee).toOther(TokenCurrencyType.usdt_omni);
        BigInteger miner_fee_total = miner_btc_fee_to_omni.getAmount().add(miner_eth_fee_to_omni.getAmount());

        BigInteger grossProfit = dto.getSum_platform_profit().add(fee).add(dto.getSum_rake())
                .subtract(dto.getSum_rebate()).subtract(dto.getAgent_rake()).subtract(dto.getInterest()).subtract(miner_fee_total);

        return FinanceDailyBreakdownVO.builder()
                .date(dto.getDate())
                .sum_platform_profit(TokenCurrencyType.usdt_omni.money(dto.getSum_platform_profit()))
                .sum_rake(TokenCurrencyType.usdt_omni.money(dto.getSum_rake()))
                .fee(TokenCurrencyType.usdt_omni.money(fee))
                .sum_rebate(TokenCurrencyType.usdt_omni.money(dto.getSum_rebate()))
                .interest(TokenCurrencyType.usdt_omni.money(dto.getInterest()))
                .agent_rake(TokenCurrencyType.usdt_omni.money(dto.getAgent_rake()))
                .miner_fee(TokenCurrencyType.usdt_omni.money(miner_fee_total))
                .miner_btc_fee(TokenCurrencyType.btc.money(miner_btc_fee))
                .miner_eth_fee(TokenCurrencyType.eth.money(miner_eth_fee))
                .gross_profit(TokenCurrencyType.usdt_omni.money(grossProfit)).build();
    }
}
