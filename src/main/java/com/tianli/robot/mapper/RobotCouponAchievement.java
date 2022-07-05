package com.tianli.robot.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RobotCouponAchievement {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 更新时间
     */
    private LocalDateTime update_time;

    /**
     * 用户的uid
     */
    private Long uid;

    /**
     * 激活码的记录id
     */
    private Long c_id;

    /**
     * 激活码
     */
    private String c_code;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 赢得次数
     */
    private Integer win_count;

    /**
     * 输的次数
     */
    private Integer lose_count;

    /**
     * 总的押注金额
     */
    private Double total_amount;

    /**
     * 盈利金额
     */
    private Double profit_amount;

    /**
     * 盈利率
     */
    private Double profit_rate;
}
