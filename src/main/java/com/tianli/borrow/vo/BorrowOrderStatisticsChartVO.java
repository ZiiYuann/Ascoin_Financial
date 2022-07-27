package com.tianli.borrow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BorrowOrderStatisticsChartVO {

    /**
     * 时间
     */
    private String time;

    /**
     * 数量
     */
    private BigDecimal amount;
}
