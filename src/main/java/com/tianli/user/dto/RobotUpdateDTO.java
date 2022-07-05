package com.tianli.user.dto;

import com.tianli.management.user.controller.CustomerUpdateDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RobotUpdateDTO {
    private Integer auto_count;
    private BigDecimal auto_amount;
    private String interval_time;
    private Double win_rate;
    private Double profit_rate;

    public static RobotUpdateDTO convert(CustomerUpdateDTO dto) {
        return RobotUpdateDTO.builder()
                .auto_count(dto.getAuto_count())
                .auto_amount(dto.getAuto_amount())
                .interval_time(dto.getInterval_time())
                .win_rate(dto.getWin_rate())
                .profit_rate(dto.getProfit_rate())
                .build();
    }
}
