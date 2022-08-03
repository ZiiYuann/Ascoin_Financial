package com.tianli.management.query;

import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.time.TimeTool;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
public class FinancialBoardQuery {

    private TimeTool.Util timeUtil;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public void calTime(){

        LocalDateTime todayZero = TimeTool.StartOfTime(TimeTool.Util.DAY);
        Optional.ofNullable(timeUtil).ifPresent(util -> {
            startTime = TimeTool.StartOfTime(timeUtil);
            endTime = todayZero.plusDays(1);
        });


       Optional.ofNullable(startTime).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException) ;
       Optional.ofNullable(endTime).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException) ;
    }
}
