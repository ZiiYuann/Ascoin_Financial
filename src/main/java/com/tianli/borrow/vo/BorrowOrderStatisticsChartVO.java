package com.tianli.borrow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BorrowOrderStatisticsChartVO implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 时间
     */
    private String time;

    /**
     * 数量
     */
    private BigDecimal amount;
}
