package com.tianli.management.vo;

import com.tianli.product.financial.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Data
public class MFinancialProductVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 币别
     */
    private String coin;

    /**
     * 产品名称
     */
    private String name;

    private String nameEn;

    private String logo;

    /**
     * 产品类型 {@link ProductType}
     */
    private ProductType type;

    /**
     * 类型 {@link PurchaseTerm}
     */
    private PurchaseTerm term;

    /**
     * 产品状态 {@link ProductStatus}
     */
    private ProductStatus status;

    private BusinessType businessType;

    /**
     * 参考年化
     */
    private BigDecimal rate;

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
     * 风险类型
     */
    private RiskType riskType;

    /**
     * 已经使用
     */
    private BigDecimal useQuota;

    /**
     * 持有用户人数
     */
    private BigInteger holdUserCount;

    private byte rateType;

    private BigDecimal maxRate;

    private BigDecimal minRate;

    private boolean recommend;

    private int recommendWeight;

    /**
     * 阶梯利率
     */
    private List<ProductLadderRateVO> ladderRates;


}
