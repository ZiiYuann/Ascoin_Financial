package com.tianli.financial.entity;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RiskType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 理财产品申购记录表
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class FinancialRecord {
    /**
     * 主键
     */
    @Id
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 理财产品id
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    private String productNameEn;

    private String logo;

    /**
     * 活期/定期产品
     */
    private ProductType productType;

    private PurchaseTerm productTerm;

    private RiskType riskType;

    private CurrencyCoin coin;

    /**
     * 状态 {@link RecordStatus}
     */
    private RecordStatus status;

    private BigDecimal holdAmount;

    /**
     * 参考年化
     */
    private BigDecimal rate;

    /**
     * 申购时间 创建时间
     */
    private LocalDateTime purchaseTime;

    /**
     * 开始计息时间
     */
    private LocalDateTime startIncomeTime;

    /**
     * 结算时间
     */
    private LocalDateTime endTime;

    /**
     * 赎回时间（最近一次）
     */
    private LocalDateTime redeemTime;

    /**
     * 自动续费
     */
    private boolean autoRenewal;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 锁定金额
     */
    private BigDecimal pledgeAmount;
}
