package com.tianli.currency_token.order.mapper;

public enum CurrencyTokenOrderStatus {
    /**
     * 已创建
     */
    created,
    /**
     * 已撤销
     */
    canceled,
    /**
     * 部分成交
     */
    partial_deal,
    /**
     * 已撤销,部分成交
     */
    canceled_partial_deal,
    /**
     * 已成交
     */
    success,
}
