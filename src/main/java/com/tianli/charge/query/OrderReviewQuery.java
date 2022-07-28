package com.tianli.charge.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewQuery {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 审核信息
     */
    private String remarks;

    /**
     * 是否审核通过
     */
    private boolean pass;

}
