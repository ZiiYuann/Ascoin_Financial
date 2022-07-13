package com.tianli.financial.entity;

import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.enums.FinancialProductType;
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
public class FinancialPurchaseRecord {
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
     * 活期/定期产品
     */
    private FinancialProductType financialProductType;

    /**
     * 理财本金
     */
    private BigDecimal amount;

    /**
     * 参考年化
     */
    private double rate;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态 {@link FinancialLogStatus}
     */
    private byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 赎回时间
     */
    private LocalDateTime finishTime;

}
