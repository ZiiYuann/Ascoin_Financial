package com.tianli.financial.entity;

import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.enums.FinancialProductType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户购买理财产品
 * </p>
 */
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class FinancialLog {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 理财产品id
     */
    private Long financialProductId;

    /**
     * 理财本金
     */
    private BigInteger amount;

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
     * 状态
     */
    private byte status;

    /**
     * 活期/定期产品
     */
    private byte financialProductType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 赎回时间
     */
    private LocalDateTime finishTime;

}
