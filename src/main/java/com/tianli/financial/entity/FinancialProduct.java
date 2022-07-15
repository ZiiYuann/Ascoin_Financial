package com.tianli.financial.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RiskType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
    @TableId
    private Long id;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品类型 {@link ProductType}
     */
    private ProductType type;

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
    private double rate;

    /**
     * 个人额度
     */
    private BigDecimal personQuota;

    /**
     * 总额度
     */
    private BigDecimal totalQuota;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
