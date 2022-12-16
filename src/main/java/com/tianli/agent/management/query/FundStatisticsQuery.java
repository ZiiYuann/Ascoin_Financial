package com.tianli.agent.management.query;

import cn.hutool.core.date.DateUtil;
import com.tianli.agent.management.enums.TimeQueryEnum;
import com.tianli.tool.time.TimeTool;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Data
public class FundStatisticsQuery {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private TimeQueryEnum timeRange;

    public void calTime() {
        if (Objects.nonNull(timeRange)) {
            if (timeRange == TimeQueryEnum.day || timeRange == TimeQueryEnum.DAY) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfDay(new Date()));
            } else if (timeRange == TimeQueryEnum.week || timeRange == TimeQueryEnum.WEEK) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfWeek(new Date()));
            } else if (timeRange == TimeQueryEnum.month ||timeRange == TimeQueryEnum.MONTH ) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfMonth(new Date()));
            }
        }
    }
}
