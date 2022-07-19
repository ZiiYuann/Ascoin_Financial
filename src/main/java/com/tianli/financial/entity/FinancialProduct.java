package com.tianli.financial.entity;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.*;
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
    private CurrencyCoin coin;

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
     * 最低申购额度
     */
    private BigDecimal limitPurchaseQuota;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
