package com.tianli.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author wangqiyun
 * @Date 2018/12/25 2:14 PM
 */
public class CommonFunction {

    public static Map<String, Object> paramMapToMap(Map<String, String[]> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }

    public static Map<String, String> paramMapToStringMap(Map<String, String[]> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }

    /**
     * 将数字拆分成n等份，用于本金以及利息的拆分
     * n必须大于0
     */
    public static List<Long> divide(long number, int n) {
        List<Long> result = new ArrayList<>(n);
        if (number <= 0L) {
            for (int i = 0; i < n; i++)
                result.add(0L);
            return result;
        }
        long base = number / n, num = number - n * base;
        for (int i = 0; i < n; i++)
            if (i < num)
                result.add(base + 1);
            else result.add(base);
        return result;
    }

    public static long parseSatoshis(String bitcoin) {
        BigDecimal value = new BigDecimal(bitcoin);
        value = value.multiply(new BigDecimal("100000000"));
        return value.longValue();
    }

    public static long roundUp(Double num) {
        long value = num.longValue();
        if (num > value) return value + 1L;
        else return value;
    }


    public static long generalId() {
        return (System.currentTimeMillis() << 20) | (ThreadLocalRandom.current().nextLong(0, 1 << 20));
    }

    public static String generalSn(long id) {
        long timestamp = id >> 20;
        return Constants.dateTimeFormatterFraction.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())) + String.format("%07d", id & 1048575L);
    }
}
