package com.tianli.charge.enums;

public enum ChargeStatus {
    created, // 新创建/待审核
    review_fail, // 审核失败
    chaining, // 进行中
    chain_fail, // 失败
    chain_success; // 成功
}
