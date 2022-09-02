package com.tianli.agent.management.query;

import com.tianli.agent.management.enums.TimeQueryEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FundStatisticsQuery {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private TimeQueryEnum timeRange;

}
