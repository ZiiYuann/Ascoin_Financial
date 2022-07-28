package com.tianli.charge.vo;

import com.tianli.charge.enums.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewVO {

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
}
