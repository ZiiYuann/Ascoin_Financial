package com.tianli.tool;

import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-25
 **/
public class ReflectTool {

    public static Object invoke(Object o, String methodName, List<?> params) {
        Class<?> aClass = o.getClass();

        try {

            if (CollectionUtils.isNotEmpty(params)) {
                Class<?>[] classes = new Class[params.size()];
                for (int i = 0; i < params.size(); i++) {
                    classes[i] = params.get(i).getClass();
                }

                Object[] objects = params.toArray();
                Method m = aClass.getDeclaredMethod(methodName, classes);
                m.setAccessible(true);
                return m.invoke(o, objects);
            }else {
                Method m = aClass.getDeclaredMethod(methodName, null);
                m.setAccessible(true);
                return m.invoke(o, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invoke(Object o, String methodName, Object param) {
        return invoke(o, methodName, List.of(param));
    }

}
