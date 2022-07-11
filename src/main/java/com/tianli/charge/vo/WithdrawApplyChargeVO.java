package com.tianli.charge.vo;

import com.tianli.charge.entity.Charge;
import com.tianli.charge.enums.ChargeStatus;
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
        LocalDateTime createTime = charge.getCreateTime();
        Instant instant = createTime.atZone(ZoneId.systemDefault()).toInstant();
        return WithdrawApplyChargeVO.builder()
                .create_time(createTime)
                .create_time_ms(instant.toEpochMilli())
                .status(charge.getStatus())
                .token(charge.getToken().name())
                .amount(charge.getCurrencyAdaptType().moneyBigDecimal(charge.getAmount()).doubleValue())
                .fee(charge.getCurrencyAdaptType().moneyBigDecimal(charge.getFee()).doubleValue())
                .real_amount(charge.getCurrencyAdaptType().moneyBigDecimal(charge.getRealAmount()).doubleValue())
                .reason(charge.getReason())
                .reason_en(charge.getReasonEn())
                .build();
    }

}
