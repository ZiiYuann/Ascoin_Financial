package com.tianli.charge.enums;

import lombok.Getter;

public enum OrderReviewStrategy {
    // 自动审核自动打币
    AUTO_REVIEW_AUTO_TRANSFER(OrderReviewType.AUTO),
    // 人工审核自动打币
    MANUAL_REVIEW_AUTO_TRANSFER(OrderReviewType.MANUAL),
    // 人工审核人工打币
    MANUAL_REVIEW_MANUAL_TRANSFER(OrderReviewType.MANUAL);

    OrderReviewStrategy(OrderReviewType reviewType) {
        this.reviewType = reviewType;
    }

    @Getter
    private final OrderReviewType reviewType;

}
