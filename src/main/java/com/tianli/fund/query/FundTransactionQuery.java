package com.tianli.fund.query;

import com.tianli.fund.enums.FundTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransactionQuery {

    private Long uid;

    private Long fundId;

    private String queryUid;

    private String queryFundId;

    private String queryProductId;

    private FundTransactionType type;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long agentId;
}
