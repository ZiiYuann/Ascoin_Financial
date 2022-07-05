package com.tianli.tool;

/**
 * Created by wangqiyun on 2018/8/1.
 */
public class FirstNotNullObject {
    public static <T> T firstNotNullObject(T... tList) {
        for (T t : tList) {
            if (t != null) return t;
        }
        return null;
    }
}
