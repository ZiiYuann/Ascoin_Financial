package com.tianli.account.enums;

/**
 * @author  wangqiyun
 * @since  2019-11-04 23:42
 */
public enum AccountOperationType {
    increase,  //充值
    reduce,    //扣除
    freeze,    //冻结
    unfreeze,  //解冻
    withdraw,  //提现
    decrease,  //非冻结扣除
    pledge, //质押
    borrow, //借款
    repay, //还款
    release //释放质押
}

