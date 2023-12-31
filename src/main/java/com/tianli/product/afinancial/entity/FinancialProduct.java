package com.tianli.product.afinancial.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tianli.product.afinancial.enums.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
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
    @Id
    private Long id;

    /**
     * 币别
     */
    private String coin;

    private String logo;

    /**
     * 产品名称
     */
    private String name;

    private String nameEn;

    /**
     * 产品类型 {@link ProductType}
     */
    private ProductType type;

    private BusinessType businessType;

    /**
     * 风险类型
     */
    private RiskType riskType;

    /**
     * 类型 {@link PurchaseTerm}
     */
    private PurchaseTerm term;

    /**
     * 产品状态 {@link ProductStatus}
     */
    private ProductStatus status;

    /**
     * 参考年化
     */
    private BigDecimal rate;

    /**
     * 个人额度
     */
    @TableField(fill = FieldFill.UPDATE)
    private BigDecimal personQuota;

    /**
     * 总额度
     */
    @TableField(fill = FieldFill.UPDATE)
    private BigDecimal totalQuota;

    /**
     * 最低申购额度
     */
    @TableField(fill = FieldFill.UPDATE)
    private BigDecimal limitPurchaseQuota;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除状态
     */
    private boolean deleted;

    /**
     * 总使用额度
     */
    private BigDecimal useQuota;

    /**
     * 利率类型 0 正常 1阶梯
     */
    private byte rateType;

    /**
     * 最大利率
     */
    private BigDecimal maxRate;

    /**
     * 最小利率
     */
    private BigDecimal minRate;

    private boolean recommend;

    private int recommendWeight;

}
