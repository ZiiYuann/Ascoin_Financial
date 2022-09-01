package com.tianli.fund.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FundIncomeQuery {

    private Long uid;

    private Long fundId;

    private String queryUid;

    private String queryProductId;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
