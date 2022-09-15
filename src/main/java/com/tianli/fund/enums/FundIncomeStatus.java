package com.tianli.fund.enums;

import lombok.Getter;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-15
 **/
public enum FundIncomeStatus {

    CAL(1),
    WAIT_REVIEW(2),
    GRANT(3),
    REVIEW_FAIL(4);

    @Getter
    private final Integer status;

    FundIncomeStatus(Integer status) {
        this.status = status;
    }

}
