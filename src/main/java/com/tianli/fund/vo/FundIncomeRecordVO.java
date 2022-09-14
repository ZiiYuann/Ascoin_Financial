package com.tianli.fund.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 基金收益记录
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
public class FundIncomeRecordVO{

    private static final long serialVersionUID=1L;

    private Long uid;

    /**
     * id
     */
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 币种
     */
    private String coin;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 持有数额
     */
    private BigDecimal holdAmount;

    /**
     * 利息数额
     */
    private BigDecimal interestAmount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * logo
     */
    private String logo;

}
