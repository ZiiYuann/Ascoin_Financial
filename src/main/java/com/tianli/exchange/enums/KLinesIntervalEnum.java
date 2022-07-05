package com.tianli.exchange.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.util.Calendar;

/**
 * @author lzy
 * @date 2022/6/14 10:49
 */
@Getter
public enum KLinesIntervalEnum {

    one("1m", KLinesRedisKey.CURRENT_K_LINE_KEY, 1), five("5m", KLinesRedisKey.CURRENT_5M_K_LINE_KEY, 5), fifteen("15m", KLinesRedisKey.CURRENT_15M_K_LINE_KEY, 15),
    thirty("30m", KLinesRedisKey.CURRENT_30M_K_LINE_KEY, 30), sixty("1h", KLinesRedisKey.CURRENT_60M_K_LINE_KEY, 60), day("1d", KLinesRedisKey.CURRENT_DAY_K_LINE_KEY, 1440);

    private String interval;

    private String redisKey;

    private Integer minute;

    KLinesIntervalEnum(String interval, String redisKey, Integer minute) {
        this.interval = interval;
        this.redisKey = redisKey;
        this.minute = minute;
    }


    public static KLinesIntervalEnum convert(String interval) {
        for (KLinesIntervalEnum value : KLinesIntervalEnum.values()) {
            if (value.getInterval().equals(interval)) {
                return value;
            }
        }
        return null;
    }


    /**
     * 获取最近一个时间间隔的开始时间
     *
     * @param kLinesIntervalEnum
     * @return
     */
    public static long getIntervalTime(KLinesIntervalEnum kLinesIntervalEnum) {
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int minute = calendar.get(Calendar.MINUTE);
        switch (kLinesIntervalEnum) {
            case one:
                return calendar.getTimeInMillis();
            case five:
                return getIntervalTime(minute, calendar, 5);
            case fifteen:
                return getIntervalTime(minute, calendar, 15);
            case thirty:
                return getIntervalTime(minute, calendar, 30);
            case sixty:
                return getIntervalTime(minute, calendar, 60);
            case day:
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                return calendar.getTimeInMillis();
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }


    private static long getIntervalTime(int minute, Calendar calendar, int range) {
        if (minute % range == 0) {
            calendar.set(Calendar.MINUTE, minute);
            return calendar.getTimeInMillis();
        }
        minute--;
        return getIntervalTime(minute, calendar, range);
    }
}
