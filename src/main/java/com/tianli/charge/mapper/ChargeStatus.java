package com.tianli.charge.mapper;

public enum ChargeStatus {
    created, // 新创建/待审核
    review_fail, // 审核失败
    chaining, // 提现中
    chain_fail, // 提现失败
    chain_success; // 提现成功
}
