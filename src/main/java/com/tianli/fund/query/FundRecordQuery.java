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

    private Long uid;

    private String queryUid;

    private Long agentUId;

    private Long productId;
}
