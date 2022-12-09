package com.tianli.charge.enums;

import lombok.Getter;

public enum OrderReviewType {
    AUTO((byte) 1),
    MANUAL((byte) 0);

    OrderReviewType(byte type) {
        this.type = type;
    }

    @Getter
    private final byte type;
}
