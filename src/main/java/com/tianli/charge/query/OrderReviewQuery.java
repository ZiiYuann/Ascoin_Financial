package com.tianli.charge.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewQuery {

    @NotBlank(message = "订单号不允许为空")
    private String orderNo;

    private String hash;

    /**
     * 审核信息
     */
    private String remarks;

    /**
     * 是否审核通过
     */
    private boolean pass;

    private String reviewBy;

    private Long rid;

}
