package com.tianli.chain.service;

import com.tianli.currency.enums.TokenAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatCollectAmount {
    private TokenAdapter currency_type;
    private BigInteger total_amount;
}
