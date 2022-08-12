package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class BorrowOrderStatisticsVO implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 借出金额
     */
    private BigDecimal borrowAmount;

    /**
     * 质押金额
     */
    private BigDecimal pledgeAmount;

    /**
     * 借款利息
     */
    private BigDecimal interestAmount;

    /**
     * 订单数量
     */
    private Integer orderNum;

}
