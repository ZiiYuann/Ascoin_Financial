package com.tianli.management.platformfinance;

import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2021-01-12 15:27
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeOverviewVO {
    private LocalDate date;
    private double totalFee;

    public static FeeOverviewVO trans(FeeDTO feeDTO){
        LocalDate date = feeDTO.getDate();
        BigInteger charge_fee_erc20 = feeDTO.getCharge_fee_erc20().toBigInteger();
        BigInteger charge_fee_omni = feeDTO.getCharge_fee_omni().toBigInteger();
        BigInteger totalFee = charge_fee_omni.add(charge_fee_erc20.multiply(new BigInteger("100")));
        return FeeOverviewVO.builder().date(date).totalFee(TokenCurrencyType.usdt_omni.money(totalFee)).build();
    }
}
