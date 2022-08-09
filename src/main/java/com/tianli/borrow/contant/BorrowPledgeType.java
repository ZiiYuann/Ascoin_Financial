package com.tianli.borrow.contant;

public interface BorrowPledgeType {

    //初始质押
    Integer INIT = 1;

    //增加
    Integer INCREASE = 2;

    //减少
    Integer REDUCE = 3;

    //平仓
    Integer LIQUIDATION = 4;
}
