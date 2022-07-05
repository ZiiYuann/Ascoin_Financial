package com.tianli.management.agentmanage.controller;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.deposit.mapper.ChargeDeposit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeDepositVO implements Serializable {
    private Long id;
    private double amount;
    private LocalDateTime create_time;
    private String note;

    public static ChargeDepositVO trans(ChargeDeposit chargeDeposit) {
        return ChargeDepositVO.builder()
                .id(chargeDeposit.getId())
                .amount(TokenCurrencyType.usdt_omni.money(chargeDeposit.getAmount()))
                .create_time(chargeDeposit.getCreate_time())
                .note(chargeDeposit.getNote())
                .build();
    }
}