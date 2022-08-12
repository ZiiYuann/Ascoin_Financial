package com.tianli.borrow.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BorrowAdjustPageVO implements Serializable {

    private static final long serialVersionUID=1L;
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
    @BigDecimalFormat("#.##%")
    private BigDecimal pledgeRate;


    @BigDecimalFormat("#.##%")
    private BigDecimal adjustPledgeRate;

}
