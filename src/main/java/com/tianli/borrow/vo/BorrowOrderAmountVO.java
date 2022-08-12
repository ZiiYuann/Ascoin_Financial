package com.tianli.borrow.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BorrowOrderAmountVO implements Serializable {

    private static final long serialVersionUID=1L;

    //累计利息
    private BigDecimal totalInterest;

    //累计借款本金
    private BigDecimal totalBorrowCapital;

    //累计待还本金
    private BigDecimal totalWaitRepayCapital;

    //累计待还利息
    private BigDecimal totalWaitRepayInterest;

}
