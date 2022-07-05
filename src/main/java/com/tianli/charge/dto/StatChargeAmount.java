package com.tianli.charge.dto;

import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatChargeAmount {
    private TokenCurrencyType currency_type;
    private BigInteger total_amount;
}