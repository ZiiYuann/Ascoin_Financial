package com.tianli.dividends.controller;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SettlementVO {
    private LocalDateTime create_time;
    private Long create_time_ms;
    private double amount;
}
