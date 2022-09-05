package com.tianli.fund.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundIncomeQuery {

    private Long uid;

    private Long fundId;

    private String queryUid;

    private String queryProductId;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long agentUId;

    private Long productId;
}
