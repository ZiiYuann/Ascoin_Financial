package com.tianli.borrow.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BorrowCoinOrderDTO {
    /**
     * 借币数量
     */
    private BigDecimal borrowAmount;
}
