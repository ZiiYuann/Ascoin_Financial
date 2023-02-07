package com.tianli.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.common.annotation.QueryWrapperGenerator;
import lombok.SneakyThrows;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public class QueryWrapperUtils {

    @SneakyThrows
    public static <T> QueryWrapper<T> generate(Class<T> tClass, Object o) {
        QueryWrapper<T> result = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>();

        Field[] fields = o.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object param = field.get(o);
            QueryWrapperGenerator queryWrapperGenerator = field.getDeclaredAnnotation(QueryWrapperGenerator.class);
            if (Objects.isNull(queryWrapperGenerator)) {
                continue;
            }
            SqlKeyword op = queryWrapperGenerator.op();

            if (Objects.isNull(param)) {
                continue;
            }

            String fieldName = queryWrapperGenerator.field();
            switch (op) {
                case EQ:
                    result = result.eq(fieldName, param);
                    break;
                case LIKE:
                    result = result.like(fieldName, param);
                    break;
                case DESC:
                    Boolean desc = (Boolean) param;
                    result = desc ? result.orderByDesc(fieldName) : result.orderByAsc(fieldName);
                    break;
                default:
                    break;
            }

        }
        return result;
    }
}
