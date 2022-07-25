package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowOrderStatisticsVO {
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
