package com.tianli.financial.enums;

import lombok.Getter;

@Getter
public enum FinancialLogStatus {

    /**
     * 申购中
     */
    PURCHASE_PROCESSING((byte) 0),
    /**
     * 申购成功
     */
    PURCHASE_SUCCESS((byte) 1),
    /**
     * 申购失败
     */
    PURCHASE_FAIL((byte) 2),
    /**
     * 记息中
     */
    INTEREST_PROCESSING((byte) 3),
    /**
     * 结算成功
     */
    SETTLE_SUCCESS((byte) 4),
    /**
     * 赎回中
     */
    REDEEM_PROCESSING((byte) 5),
    /**
     * 赎回成功
     */
    REDEEM_SUCCESS((byte) 6),
    /**
     * 转存成功
     */
    TRANSFER_SUCCESS((byte) 7);


    FinancialLogStatus(byte type) {
        this.type = type;
    }

    private final byte type;
}
