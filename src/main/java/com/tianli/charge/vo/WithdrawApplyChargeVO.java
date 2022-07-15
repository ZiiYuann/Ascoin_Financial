package com.tianli.charge.vo;

import com.tianli.charge.entity.Order;
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


}
