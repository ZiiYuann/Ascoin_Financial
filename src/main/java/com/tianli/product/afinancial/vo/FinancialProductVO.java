package com.tianli.product.afinancial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tianli.chain.vo.CoinVO;
import com.tianli.management.vo.ProductLadderRateVO;
import com.tianli.product.afinancial.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-12
 **/
@Data
public class FinancialProductVO {

    /**
     * 主键
     */
    @TableId
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
     * 个人已使用额度
     */
    private BigDecimal userPersonQuota;

    /**
     * 个人额度
     */
    private BigDecimal personQuota;

    /**
     * 已经使用
     */
    private BigDecimal useQuota;

    /**
     * 总额度
     */
    private BigDecimal totalQuota;

    /**
     * 申购时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime purchaseTime;

    /**
     * 已经使用基础额度（假数据）
     */
    private BigDecimal baseUseQuota;

    /**
     * 记息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startIncomeTime;

    /**
     * 结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime settleTime;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 最低申购额度
     */
    private BigDecimal limitPurchaseQuota;

    /**
     * 是否允许申购
     */
    private boolean allowPurchase;

    /**
     * 风险类型
     */
    private RiskType riskType;

    /**
     * 利率类型
     */
    private byte rateType;

    /**
     * 持有金额 == userPersonQuota 字端，前端不理解，额外加一个便于他们理解
     */
    private BigDecimal holdAmount;

    /**
     * 可以赎回的recordId
     */
    private Long recordId;

    private BigDecimal maxRate;

    private BigDecimal minRate;

    /**
     * 是否售罄
     */
    private boolean sellOut;

    /**
     * 是否持有
     */
    private boolean hold;

    private boolean newUser;

    /**
     * 币别信息
     */
    private List<CoinVO> coins;

    /**
     * 阶梯化利率
     */
    private List<ProductLadderRateVO> ladderRates;


}
