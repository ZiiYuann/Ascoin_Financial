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
    private String queryUid;

    private Long agentId;

    private Long productId;
}
