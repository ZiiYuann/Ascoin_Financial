package com.tianli.charge.controller;

import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawApplyChargeVO {
    private LocalDateTime create_time;
    private Long create_time_ms;
    private ChargeStatus status;
    private double amount;
    private double fee;
    private double real_amount;
    private String token;
    private String reason;
    private String reason_en;
    private String chain;

    public static WithdrawApplyChargeVO trans(Charge charge) {
        LocalDateTime create_time = charge.getCreateTime();
        Instant instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        return WithdrawApplyChargeVO.builder()
                .create_time(create_time)
                .create_time_ms(instant.toEpochMilli())
                .status(charge.getStatus())
                .token(charge.getToken().voStr())
                .amount(charge.getCurrencyType().money(charge.getAmount()))
                .fee(charge.getCurrencyType().money(charge.getFee()))
                .real_amount(charge.getCurrencyType().money(charge.getRealAmount()))
                .reason(charge.getReason())
                .reason_en(charge.getReasonEn())
                .build();
    }

}
