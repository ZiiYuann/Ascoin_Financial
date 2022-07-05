package com.tianli.financial.mapper;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UserFinancialLog {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long user_id;

    /**
     * 理财产品id
     */
    private Long financial_product_id;

    /**
     * 活期/定期产品
     */
    private String financial_product_type;

    /**
     * 理财本金
     */
    private BigInteger amount;

    /**
     * 日利率
     */
    private double rate;

    /**
     * 开始日期
     */
    private LocalDate start_date;

    /**
     * 结束日期
     */
    private LocalDate end_date;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 赎回时间
     */
    private LocalDateTime finish_time;

    /**
     * 赎回金额
     */
    private BigInteger finish_amount;
}
