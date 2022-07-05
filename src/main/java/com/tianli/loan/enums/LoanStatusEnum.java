package com.tianli.loan.enums;

import lombok.Getter;

/**
 * @author lzy
 * @date 2022/5/26 11:21
 */
@Getter
public enum LoanStatusEnum {
    /**
     * 待审核
     */
    PENDING_REVIEW,
    /**
     * 审核失败
     */
    AUDIT_FAILURE,
    /**
     * 使用中
     */
    USING,
    /**
     * 已到期
     */
    BE_EXPIRED,
    /**
     * 已逾期
     */
    PAST_DUE,
    /**
     * 已还款
     */
    REPAID;

    public static LoanStatusEnum getLoanStatusEnum(String status) {
        for (LoanStatusEnum value : LoanStatusEnum.values()) {
            if (value.name().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
