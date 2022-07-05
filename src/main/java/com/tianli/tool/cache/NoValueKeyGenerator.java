package com.tianli.tool.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by wangqiyun on 2018/8/10.
 */
@Component("noValueKeyGenerator")
public class NoValueKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object o, Method method, Object... objects) {
        return "";
    }
}
