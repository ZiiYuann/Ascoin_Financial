package com.tianli.management.platformfinance;

import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2020-12-21 16:53
 * @since 1.0.0
 */
@Data
@Builder
public class FinanceExhibitionVO {
    private LocalDate createTime;
    private Double platformProfit;
    private Double fee;
    private Double rebate;
    private Double agentProfit;
    private Double agentSettled;

    public static FinanceExhibitionVO trans(FinanceExhibitionDTO dto){
        BigInteger feeErc20 = dto.getUserFeeErc20().add(dto.getDepositFeeErc20()).add(dto.getSettlementFeeErc20());
        BigInteger feeOmni = dto.getUserFeeOmni().add(dto.getDepositFeeOmni()).add(dto.getSettlementFeeOmni());
        BigInteger fee = feeOmni.add(feeErc20.multiply(new BigInteger("100")));
        return FinanceExhibitionVO.builder().createTime(dto.getDate())
                .platformProfit(TokenCurrencyType.usdt_omni.money(dto.getPlatform_profit()))
                .fee(TokenCurrencyType.usdt_omni.money(fee))
                .rebate(TokenCurrencyType.usdt_omni.money(dto.getRebate_amount()))
                .agentProfit(TokenCurrencyType.usdt_omni.money(dto.getAgent_dividends()))
                .agentSettled(TokenCurrencyType.usdt_omni.money(dto.getSettled_number())).build();
    }
}
