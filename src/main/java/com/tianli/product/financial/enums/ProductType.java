package com.tianli.product.financial.enums;

import lombok.Getter;

@Getter
public enum ProductType {
    // current 活期  fixed 定期
    /**
     *  定期
     */
    fixed((byte) 0),
    /**
     * 活期
     */
    current((byte) 1),

    /**
     *基金
     */
    fund((byte)2);

    ProductType(byte type) {
        this.type = type;
    }

    private final byte type;

}
