package com.tianli.management.platformfinance;

import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chensong
 * @date 2020-12-21 09:40
 * @since 1.0.0
 */
@Data
@Builder
public class FeeVO {
    private LocalDate createTime;
    private Double userFee;

    public static FeeVO trans(FeeDTO feeDTO){
        LocalDate date = feeDTO.getDate();
        BigInteger charge_fee_erc20 = feeDTO.getCharge_fee_erc20().toBigInteger();
        BigInteger charge_fee_omni = feeDTO.getCharge_fee_omni().toBigInteger();
        BigInteger userFee = charge_fee_omni.add(charge_fee_erc20.multiply(new BigInteger("100")));

        return FeeVO.builder().createTime(date)
                .userFee(TokenCurrencyType.usdt_omni.money(userFee))
                .build();

    }
}
