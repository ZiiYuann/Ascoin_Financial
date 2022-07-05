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
public class RobotCoupon {

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
     * 激活码
     */
    private String activation_code;

    /**
     * 总数量
     */
    private Double total_amount;

    /**
     * 自动押注的次数
     */
    private Integer auto_count;

    /**
     * 押注的金额
     */
    private Double auto_amount;

    /**
     * 时间间隔区间
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

    /**
     * 0 未使用  1 正在使用  2 已使用
     */
    private Integer status;

    /**
     * -1 表示没使用   其他为使用者的用户id
     */
    private Long uid;
    private String username;

    /**
     * 已经使用的次数
     */
    private Integer used_count;

    /**
     * 使用时间
     */
    private LocalDateTime used_time;

    /**
     * 管理员
     */
    private String opt_admin;

    /**
     * 0: 未处理
     * 1: 已处理
     */
    private Integer scan;

}
