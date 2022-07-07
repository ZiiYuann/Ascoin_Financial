package com.tianli.financial.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.FinancialProductType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 理财产品
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
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
    private String nameEn;

    /**
     * 参考年化
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
    private String descriptionEn;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 类型 {@link PurchaseTerm}
     */
    private byte term;

    /**
     * 产品状态 {@link FinancialProductStatus}
     */
    private byte status;

    /**
     * 产品类型 {@link FinancialProductType}
     */
    private byte type;
}
