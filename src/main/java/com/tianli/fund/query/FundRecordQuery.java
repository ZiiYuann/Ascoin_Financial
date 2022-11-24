package com.tianli.fund.query;

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
     * 持有用户对uid
     */
    private String queryUid;

    /**
     * 代理人id
     */
    private Long agentId;


    private Long productId;

    private String coin;

    public FundRecordQuery(Long productId) {
        this.productId = productId;
    }


}
