package com.tianli.charge.entity;

import com.tianli.charge.enums.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * 订单审核表
 *
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReview {

    @Id
    private Long id;

    /**
     * 审核人id
     */
    private Long rid;

    /**
     * 审核备注
     */
    private String remarks;

    /**
     * 审核状态
     */
    private ChargeStatus status;

    /**
     * 审核时间
     */
    private LocalDateTime createTime;

    /**
     * 0：人共审核 1：自动审核
     */
    private byte type;
}
