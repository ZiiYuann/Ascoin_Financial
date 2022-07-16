package com.tianli.common;

import java.time.*;


/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
public class TimeUtils {

    public static LocalDateTime StartOfTime(Util timeUtil){
        LocalDate date;
        LocalDate now = LocalDate.now();
        switch (timeUtil){
            case DAY: date = now; break;
            case WEEK:
                int week = now.getDayOfWeek().getValue();
                date = now.minusDays(week - 1);
                break;
            case MONTH:
                int month = now.getDayOfMonth();
                date = now.minusDays(month - 1);
                break;
            case YEAR:
                int year = now.getDayOfYear();
                date = now.minusDays(year - 1);
                break;
            default:
                throw new IllegalArgumentException();
        }

       return LocalDateTime.of(date, LocalTime.MIN);
    }

    public static long toTimestamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(long timestamp){
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }

    public enum Util {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    public static void main(String[] args) {
        System.out.println("本日开始0点：" + TimeUtils.StartOfTime(Util.DAY));
        System.out.println("本周开始0点：" + TimeUtils.StartOfTime(Util.WEEK));
        System.out.println("本月开始0点"+ TimeUtils.StartOfTime(Util.MONTH));
        System.out.println("本年开始0点"+ TimeUtils.StartOfTime(Util.YEAR));
    }

}
