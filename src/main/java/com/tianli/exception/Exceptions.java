package com.tianli.exception;

import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqiyun on 16/9/13.
 */
public class Exceptions {
    private static final Map<Class, ErrCodeException> e = new HashMap<>();

    static {
        e.put(HttpMessageNotReadableException.class, new ErrCodeException(102, "参数错误"));
        e.put(javax.validation.ConstraintViolationException.class, new ErrCodeException(102, "参数错误"));
    }

    public static Map<Class, ErrCodeException> getE() {
        return e;
    }
}
