package com.tianli.currency.log;

/**
 * @Author wangqiyun
 * @Date 2019-11-04 23:42
 */
public enum CurrencyLogType {
    increase,  //增加
    reduce,    //扣除
    freeze,    //冻结
    unfreeze,  //解冻
    withdraw,  //提现
    decrease,  //非冻结扣除
}
