package com.tianli.financial.enums;

import lombok.Getter;

@Getter
public enum FinancialProductType {

    /**
     *  定期
     */
    current((byte) 0),
    /**
     * 活期
     */
    fixed((byte) 1);

    FinancialProductType(byte type) {
        this.type = type;
    }

    private final byte type;

}
