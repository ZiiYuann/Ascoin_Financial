package com.tianli.financial.enums;

import lombok.Getter;

@Getter
public enum ProductType {
    // current 定期 fixed 活期
    /**
     *  定期
     */
    current((byte) 0),
    /**
     * 活期
     */
    fixed((byte) 1);

    ProductType(byte type) {
        this.type = type;
    }

    private final byte type;

}
