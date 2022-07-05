package com.tianli.management.actvccode;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RobotCouponUpdateReq {

    /**
     * 主键
     */
    private Long id;

    /**
     * 总数量
     */
    @NotNull
    private Double total_amount;

    /**
     * 自动押注的次数
     */
    @NotNull
    private Integer auto_count;

    /**
     * 押注的金额
     */
    @NotNull
    private Double auto_amount;

    /**
     * 时间间隔区间
     */
    @NotBlank
    private String interval_time;

    /**
     * 胜率
     */
    @NotNull
    private Double win_rate;

    /**
     * 利润率
     */
    @NotNull
    private Double profit_rate;
}
