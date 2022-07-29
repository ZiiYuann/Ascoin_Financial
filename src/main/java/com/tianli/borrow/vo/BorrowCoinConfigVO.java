package com.tianli.borrow.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BorrowCoinConfigVO {
    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 币种
     */
    private String coin;

    /**
     * 最小可借
     */
    private BigDecimal minimumBorrow;

    /**
     * 最大可借
     */
    private BigDecimal maximumBorrow;

    /**
     * 年利率
     */
    private BigDecimal annualInterestRate;
}
