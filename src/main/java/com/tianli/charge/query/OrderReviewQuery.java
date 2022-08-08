package com.tianli.charge.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Data
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

}
