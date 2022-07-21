package com.tianli.borrow.contant;

public interface BorrowOrderStatus {

    //计息
    Integer INTEREST_ACCRUAL = 1;
    //还款成功
    Integer SUCCESSFUL_REPAYMENT = 2;
    //强制平仓
    Integer FORCED_LIQUIDATION = 3;

}
