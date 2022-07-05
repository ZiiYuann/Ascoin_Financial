package com.tianli.robot.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RobotSaveDTO {

    /**
     * 总的次数
     */
    private Integer auto_count;

    /**
     * 自动押注的金额
     */
    private BigDecimal auto_amount;

    /**
     * 间隔时间
     */
    private String interval_time;

    /**
     * 胜率
     */
    private Double win_rate;

    /**
     * 利润率
     */
    private Double profit_rate;
}
