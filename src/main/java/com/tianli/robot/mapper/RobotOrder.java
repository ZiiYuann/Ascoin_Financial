package com.tianli.robot.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RobotOrder {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    private Long uid;
    private Integer count;
    private BigDecimal amount;
    private String interval_time;
    private String coin;
    private Integer date;
    private Boolean status;
    /**
     * 下次押注时间
     */
    private Long next_bet_time;

    /**
     * 机器人的当前编码
     */
    private Long robot_code;

}
