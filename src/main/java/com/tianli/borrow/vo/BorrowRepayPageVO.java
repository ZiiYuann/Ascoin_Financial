package com.tianli.borrow.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowRepayPageVO {

    private BigDecimal availableBalance;

    private BigDecimal totalRepayAmount;

}
