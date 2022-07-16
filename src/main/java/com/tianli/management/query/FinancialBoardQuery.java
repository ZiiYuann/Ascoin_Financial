package com.tianli.management.query;

import com.tianli.common.TimeUtils;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
public class FinancialBoardQuery {

    private TimeUtils.Util timeUtil;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public void calTime(){
        Optional.ofNullable(timeUtil).ifPresent(util -> {
            startTime = TimeUtils.StartOfTime(timeUtil);
            endTime = startTime.plusDays(1);
        });

       Optional.ofNullable(startTime).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException) ;
       Optional.ofNullable(endTime).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException) ;
    }
}
