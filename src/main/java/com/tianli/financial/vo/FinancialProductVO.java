package com.tianli.financial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private CurrencyCoin coin;

    /**
     * 产品名称
     */
    private String name;

    private String nameEn;

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

    /**
     * 参考年化
     */
    private double rate;

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
    private LocalDateTime purchaseTime;

    /**
     * 记息时间
     */
    private LocalDateTime startIncomeTime;

    /**
     * 结算时间
     */
    private LocalDateTime settleTime;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 最低申购额度
     */
    private BigDecimal limitPurchaseQuota;
}
