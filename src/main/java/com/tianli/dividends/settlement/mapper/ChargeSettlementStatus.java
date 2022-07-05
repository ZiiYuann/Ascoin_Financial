package com.tianli.dividends.settlement.mapper;

public enum ChargeSettlementStatus {
    created, // 新创建/待审核
    review_fail, // 审核失败
    transacting, // 交易中
    transaction_fail, // 交易失败
    transaction_success; //交易成功
}