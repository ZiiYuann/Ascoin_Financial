package com.tianli.common.init;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 用户信息上下文
 * @author haoding
 */
@Slf4j
public class TspContent {

    private static ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取线程内部数据
     */
    public static Map<String, Object> get() {
        Map<String, Object> cache = THREAD_LOCAL.get();
        if(Objects.isNull(cache)){
            cache = new HashMap<>();
            THREAD_LOCAL.set(cache);
        }
        return cache;
    }

    /**
     * 获取线程内部数据
     */
    public static Object get(String key) {
        Map<String, Object> cache = get();
        return cache.get(key);
    }

    /**
     * 设置线程内部数据
     */
    public static void set(String key, Object o) {
        Map<String, Object> cache = get();
        cache.put(key, o);
    }

    /**
     * 移除管理员信息
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

}