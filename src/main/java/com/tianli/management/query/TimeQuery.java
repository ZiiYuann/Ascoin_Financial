package com.tianli.management.query;

import cn.hutool.core.date.DateUtil;
import com.tianli.agent.management.enums.TimeQueryEnum;
import com.tianli.tool.time.TimeTool;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-10
 **/
@Data
public class TimeQuery {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private TimeQueryEnum timeRange;

    public void calTime() {
        if (Objects.nonNull(timeRange)) {
            if (timeRange == TimeQueryEnum.day) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfDay(new Date()));
            } else if (timeRange == TimeQueryEnum.week) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfWeek(new Date()));
            } else if (timeRange == TimeQueryEnum.month) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfMonth(new Date()));
            }
            if (Objects.isNull(endTime)){
                endTime = LocalDateTime.now();
            }
        }
    }
}
