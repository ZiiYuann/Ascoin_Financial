package com.tianli.tool.time;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimeTool {

    /**
     * day当天开始时间
     */
    public static LocalDateTime minDay(LocalDateTime day){
        return day.with(LocalTime.MIN);
    }

    /**
     * day当天结束时间
     */
    public static LocalDateTime maxDay(LocalDateTime day){
        return day.with(LocalTime.MAX);
    }

    /**
     * day本月开始时间
     */
    public static LocalDateTime minMonthTime(LocalDateTime day){
        LocalDate today = day.toLocalDate();
        Month month = today.getMonth();
        LocalDate min = LocalDate.of(today.getYear(), month, 1);
        return min.atTime(0, 0, 0);
    }

    /**
     * day本年开始时间
     */
    public static LocalDateTime minYearTime(LocalDateTime day){
        int year = day.getYear();
        return LocalDateTime.of(year, 1, 1, 0, 0, 0);
    }

    /**
     * day本月最大时间
     */
    public static LocalDateTime maxMonthTime(LocalDateTime day){
        LocalDate today = day.toLocalDate();
        Month month = today.getMonth();
        int length = month.length(today.isLeapYear());
        LocalDate max = LocalDate.of(today.getYear(), month, length);
        return max.atTime(23, 59, 59);
    }

    /**
     * 当前时间的当天开始时间结束时间
     */
    public static Map<String, LocalDateTime> thisDay(){
        Map<String, LocalDateTime> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        map.put("start", minDay(now));
        map.put("end", maxDay(now));
        return map;
    }

    /**
     * 一周维度: 周一到周日
     * 当前时间的本周开始时间结束时间
     */
    public static Map<String, LocalDateTime> thisWeekMondayToSunday(){
        LocalDate today = LocalDate.now();
        DayOfWeek week = today.getDayOfWeek();
        int value = week.getValue();
        LocalDate min = today.minusDays(value - 1);
        LocalDate max = today.plusDays(7 - value);
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", min.atTime(0, 0, 0));
        map.put("end", max.atTime(23, 59, 59));
        return map;
    }
    /**
     * 一周维度: 周日到周六
     * 当前时间的本周开始时间结束时间
     */
    public static Map<String, LocalDateTime> thisWeekSundayToSaturday(){
        LocalDate today = LocalDate.now();
        DayOfWeek week = today.getDayOfWeek();
        int value = week.getValue();
        LocalDate min = today.minusDays(value);
        LocalDate max = today.plusDays(7 - value - 1);
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", min.atTime(0, 0, 0));
        map.put("end", max.atTime(23, 59, 59));
        return map;
    }

    /**
     * 当前时间的本月开始时间结束时间
     */
    public static Map<String, LocalDateTime> thisMonth(){
        LocalDateTime today = LocalDateTime.now();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("start", minMonthTime(today));
        map.put("end", maxMonthTime(today));
        return map;
    }
    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static Map<String, String> theLast15DaysStr(){
        Map<String, LocalDate> last15Days = theLast15Days();
        Map<String, String> map = new HashMap<>();
        map.put("today", last15Days.get("today").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        map.put("pastDays", last15Days.get("pastDays").format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return map;
    }

    public static Map<String, LocalDate> theLast15Days(){
        return theLastNDays(15);
    }

    public static Map<String, LocalDate> theLastNDays(int n){
        LocalDate today = LocalDate.now();
        LocalDate minusDays = today.minusDays(n);
        Map<String, LocalDate> map = new HashMap<>();
        map.put("today", today);
        map.put("pastDays", minusDays);
        return map;
    }
    /**
     * 格式化时间(yyyy-MM-dd HH:mm:ss)
     * @param dateTime 日期时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeDisplayString(LocalDateTime dateTime) {
        return getDateTimeDisplayString(dateTime, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 格式化时间
     * @param dateTime 时间localDateTime
     * @param dfStr 格式
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeDisplayString(LocalDateTime dateTime, String dfStr) {
        if(Objects.isNull(dateTime)){
            return "";
        }
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern(dfStr);
        return dtf2.format(dateTime);
    }

    public static int getNowInteger() {
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() * 10000 + now.getMonth().getValue() * 100 + now.getDayOfMonth();
    }

    /**
     * LocalDateTime转 Date
     * @param date
     * @return
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     *  Date转LocalDateTime
     * @param localDateTime
     * @return
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate dateToLocalDate(Date date) {
        if (Objects.isNull(date))return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void sleep(long time, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    public static long toTimestamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static LocalDateTime StartOfTime(TimeTool.Util timeUtil){
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

    public enum Util {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
