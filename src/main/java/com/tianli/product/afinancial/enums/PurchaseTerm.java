package com.tianli.product.afinancial.enums;

import lombok.Getter;

@Getter
public enum PurchaseTerm {
    // ONE_WEEK 七天 TWO_WEEK 半个月 ONE_MONTH 一个月 TWO_MONTH 2个月 THREE_MONTH  三个月
    ONE_WEEK( 7),
    TWO_WEEK( 14),
    ONE_MONTH( 30),
    TWO_MONTH(60),
    THREE_MONTH( 90),
    FOUR_MONTH(120),
    SIX_MONTH( 180),
    NONE(1);

    PurchaseTerm( int day) {
        this.day = day;
    }

    private final int day;
}
