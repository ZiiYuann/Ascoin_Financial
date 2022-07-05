package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 理财产品
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class FinancialProduct {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 图片
     */
    private String logo;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 英文产品名称
     */
    private String name_en;

    /**
     * 日利率
     */
    private double rate;

    /**
     * 锁仓天数
     */
    private Long period;

    /**
     * 累计投资
     */
    private BigInteger all_invest;

    /**
     * 描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String description_en;

    /**
     * 类型
     */
    private String type;

    /**
     * 添加时间
     */
    private LocalDateTime create_time;
    /**
     * 修改时间
     */
    private LocalDateTime update_time;

    /**
     * status
     */
    private String status;
}
