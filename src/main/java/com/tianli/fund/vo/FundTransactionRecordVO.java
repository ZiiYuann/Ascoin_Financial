package com.tianli.fund.vo;

import com.tianli.fund.enums.FundTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransactionRecordVO implements Serializable {
    private static final long serialVersionUID=1L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单ID
     */
    private Long fundId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品英文名称
     */
    private String productNameEn;

    /**
     * 交易类型
     */
    private FundTransactionType type;

    /**
     * 币种
     */
    private String coin;

    /**
     * 年化率
     */
    private BigDecimal rate;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 交易数额
     */
    private BigDecimal transactionAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 预计收益
     */
    private BigDecimal expectedIncome;

    /**
     * 到账时间
     */
    private LocalDateTime accountTate;
}
