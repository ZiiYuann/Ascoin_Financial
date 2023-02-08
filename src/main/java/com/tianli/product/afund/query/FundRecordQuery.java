package com.tianli.product.afund.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundRecordQuery {

    /**
     * uid模糊查询
     */
    private String queryUid;

    /**
     * uid精确查询
     */
    private Long uid;

    /**
     * 代理人id
     */
    private Long agentId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 币别
     */
    private String coin;



    public FundRecordQuery(Long productId) {
        this.productId = productId;
    }


}
