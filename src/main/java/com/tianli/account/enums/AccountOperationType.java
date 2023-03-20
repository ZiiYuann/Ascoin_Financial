package com.tianli.account.enums;

/**
 * @author  wangqiyun
 * @since  2019-11-04 23:42
 */
public enum AccountOperationType {
    increase,  // 增加
    reduce,    // 减少
    freeze,    // 冻结
    unfreeze,  // 解冻
    decrease,  // 非冻结扣除
    withdraw, //
    pledge_freeze,
    pledge_unfreeze,
    pledge_reduce
}

