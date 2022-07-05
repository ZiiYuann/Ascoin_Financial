package com.tianli.exchange.util;

import java.util.Calendar;

/**
 * @author lzy
 * @date 2022/6/28 16:09
 */
public class CalendarUtil {

    private CalendarUtil(){}


    /**
     * 获取当前分的毫秒
     * @param amount 可以选择加amount分的时间
     * @return
     */
    public static long getM(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取当前天的毫秒
     * @return
     */
    public static long getCurrentTimeMs() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }
}
