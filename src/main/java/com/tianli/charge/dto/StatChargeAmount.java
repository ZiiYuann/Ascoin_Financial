package com.tianli.charge.dto;

import com.tianli.currency.enums.CurrencyAdaptType;
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
    private CurrencyAdaptType currency_type;
    private BigInteger total_amount;
}