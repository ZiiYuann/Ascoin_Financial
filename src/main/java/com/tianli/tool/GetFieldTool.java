package com.tianli.tool;



import com.tianli.exception.ErrCodeException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangqiyun on 2018/7/17.
 */
public class GetFieldTool {
    public static Object get(Class c, String field, Object target) {
        Field field1 = getField(c, field);
        try {
            return field1.get(target);
        } catch (IllegalAccessException e) {
            throw new ErrCodeException();
        }
    }

    private static Field getField(Class c, String field) {
        try {
            Map<String, Field> map;
            if (fieldMap.containsKey(c)) {
                map = fieldMap.get(c);
            } else {
                map = new ConcurrentHashMap<>();
                fieldMap.put(c, map);
            }
            Field declaredField;
            if (map.containsKey(field)) {
                declaredField = map.get(field);
            } else {
                declaredField = c.getDeclaredField(field);
                map.put(field, declaredField);
                declaredField.setAccessible(true);
            }
            return declaredField;
        } catch (NoSuchFieldException e) {
            throw new ErrCodeException();
        }
    }

    private static final Map<Class, Map<String, Field>> fieldMap = new ConcurrentHashMap<>();
}
