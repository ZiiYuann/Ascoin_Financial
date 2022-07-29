package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowAdjustPageVO {

    /**
     * 可用数额
     */
    private BigDecimal availableAmount;

    /**
     * 允许减少数额
     */
    private BigDecimal ableReduceAmount;

    /**
     * 当前质押率
     */
    private BigDecimal pledgeRate;

}
