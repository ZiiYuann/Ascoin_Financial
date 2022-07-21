package com.tianli.borrow.contant;


import java.math.BigDecimal;

public interface BorrowPledgeRate {

    //初始质押率
    BigDecimal INITIAL_PLEDGE_RATE = BigDecimal.valueOf(0.65) ;

    //预警质押率
    BigDecimal WARN_PLEDGE_RATE = BigDecimal.valueOf(0.8);

    //强制平仓质押率
    BigDecimal LIQUIDATION_PLEDGE_RATE = BigDecimal.valueOf(0.95);

}
