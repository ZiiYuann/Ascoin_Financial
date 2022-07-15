package com.tianli.charge.entity;

import java.time.LocalDateTime;

/**
 * 订单审核表
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
public class OrderReviewer {

    /**
     * 审核人
     */
    private String reviewer;

    /**
     * 审核人id
     */
    private Long reviewerUid;

    /**
     * 审核备注
     */
    private String reviewNote;

    /**
     * 审核时间
     */
    private LocalDateTime reviewerTime;
}
