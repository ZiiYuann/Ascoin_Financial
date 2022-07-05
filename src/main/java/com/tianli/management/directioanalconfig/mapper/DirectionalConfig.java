package com.tianli.management.directioanalconfig.mapper;

import com.tianli.bet.mapper.BetResultEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 *  2021-03-04 11:06
 * @since 1.0.0
 */
@Data
@Builder
public class DirectionalConfig {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 币种类型
     */
    private String currency_type;

    /**
     * 开始时间
     */
    private LocalDateTime start_time;

    /**
     * 结束时间
     */
    private LocalDateTime end_time;

    /**
     * 管理员id
     */
    private Long admin_id;

    /**
     * 管理员名称
     */
    private String admin_username;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 备注
     */
    private String remark;

    /**
     * 押注结果走向
     */
    private BetResultEnum result;
}
