package com.tianli.management.user.controller;

import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerChargeVO {
    private Long id;
    private LocalDateTime create_time;
    private double amount;
    private double fee;
    private double real_amount;
    private ChargeStatus status;
    private String to_address;
    private String txid;
    private CurrencyTokenEnum token;

    public static CustomerChargeVO trans(Charge charge) {
        TokenCurrencyType currency_type = charge.getCurrency_type();
        BigInteger real_amount = BigInteger.ZERO;
        if(ChargeStatus.chain_success.equals(charge.getStatus())){
            real_amount = charge.getReal_amount();
        }
        return CustomerChargeVO.builder()
                .id(charge.getId())
                .create_time(charge.getCreate_time())
                .amount(currency_type.money(charge.getAmount()))
                .fee(currency_type.money(charge.getFee()))
                .real_amount(currency_type.money(real_amount))
                .status(charge.getStatus())
                .to_address(charge.getTo_address())
                .txid(charge.getTxid())
                .token(charge.getToken())
                .build();
    }
}