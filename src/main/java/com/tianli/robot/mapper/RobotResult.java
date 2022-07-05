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
public class RobotResult {

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
     * 0输 1赢
     */
    private Integer bet_result;

    /**
     * 0未使用  1使用
     */
    private Integer status;

    /**
     * -1表示位置  1 - n, 表示第n次到第一次的下注下标
     */
    private Long bet_index;

    /**
     * 机器人编码
     */
    private Long robot_code;
}
