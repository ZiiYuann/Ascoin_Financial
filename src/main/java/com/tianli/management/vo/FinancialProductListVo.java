package com.tianli.management.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/1 5:51 下午
 */
@Data
public class FinancialProductListVo {

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
