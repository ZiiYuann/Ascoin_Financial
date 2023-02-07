package com.tianli.product.aborrow.enums;

import lombok.Getter;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public enum BorrowStatus {
    CLOSE(0),
    OPEN(1);


    BorrowStatus(int status) {
        this.status = status;
    }

    public static BorrowStatus valueOf(int status) {
        BorrowStatus[] values = BorrowStatus.values();
        for (BorrowStatus borrowStatus : values) {
            if (borrowStatus.getStatus() == status) {
                return borrowStatus;
            }
        }
        return null;
    }

    @Getter
    private final int status;
}
