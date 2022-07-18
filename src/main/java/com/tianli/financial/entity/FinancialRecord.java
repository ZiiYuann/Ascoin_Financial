package com.tianli.financial.entity;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.ProductType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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

    /**
     * 活期/定期产品
     */
    private ProductType productType;

    private PurchaseTerm productTerm;

    private CurrencyCoin coin;

    /**
     * 状态 {@link RecordStatus}
     */
    private RecordStatus status;

    private BigDecimal holdAmount;

    /**
     * 参考年化
     */
    private double rate;

    /**
     * 开始日期
     */
    private LocalDate startTime;

    /**
     * 结束日期
     */
    private LocalDate endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 赎回时间
     */
    private LocalDateTime redeemTime;

}
