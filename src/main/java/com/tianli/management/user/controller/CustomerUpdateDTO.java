package com.tianli.management.user.controller;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class CustomerUpdateDTO {
    @NotNull(message = "剩余次数为空")
    private Integer auto_count;
    @NotNull(message = "单笔金额为空")
    private BigDecimal auto_amount;
    @NotBlank(message = "下单时间间隔为空")
    private String interval_time;
    @NotNull(message = "胜率为空")
    private Double win_rate;
    @NotNull(message = "利润率为空")
    private Double profit_rate;
}
