package com.tianli.fund.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.fund.enums.FundTransactionType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
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
     * 交易类型
     */
    private FundTransactionType type;

    /**
     * 币种
     */
    private CurrencyCoin coin;

    /**
     * 年化率
     */
    private BigDecimal rate;

    /**
     * 审核结果
     */
    private Boolean auditResult;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

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
}
