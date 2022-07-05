package com.tianli.currency;

import lombok.Getter;

/**
 * 余额类型
 */
@Getter
public enum CurrencyTypeEnum {
    normal, // 普通余额
    deposit, // 押金余额
    settlement,//抽水
    financial,  //  理财
    actual,  // 现货
    loan,//贷款
}
