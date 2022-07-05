package com.tianli.tool.cache;

import com.google.gson.Gson;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by wangqiyun on 2018/8/7.
 */

/**
 * 给spring cahce使用的工具类
 */
@Component("jsonKeyGenerator")
public class JsonKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object o, Method method, Object... objects) {
        return new Gson().toJson(objects);
    }
}
