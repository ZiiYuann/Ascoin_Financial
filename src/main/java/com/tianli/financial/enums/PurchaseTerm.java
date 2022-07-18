package com.tianli.financial.enums;

import lombok.Getter;

@Getter
public enum PurchaseTerm {
    // ONE_WEEK 七天 TWO_WEEK 半个月 ONE_MONTH 一个月 TWO_MONTH 2个月 THREE_MONTH  三个月
    ONE_WEEK((byte) 0, 7),
    TWO_WEEK((byte) 1, 14),
    ONE_MONTH((byte) 2, 30),
    TWO_MONTH((byte) 3, 60),
    THREE_MONTH((byte) 4, 90);

    PurchaseTerm(byte type, int day) {
        this.type = type;
        this.day = day;
    }

    private final byte type;
    private final int day;
}
