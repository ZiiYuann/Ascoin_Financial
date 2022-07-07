package com.tianli.financial.enums;

import lombok.Getter;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
@Getter
public enum FinancialProductStatus {
    enable((byte) 0);

    FinancialProductStatus(byte type) {
        this.type = type;
    }

    private final byte type;
}
