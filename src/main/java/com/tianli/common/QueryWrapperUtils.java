package com.tianli.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.common.query.SelectQuery;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.tron.tronj.abi.datatypes.Array;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public class QueryWrapperUtils {

    @SneakyThrows
    public static <T> QueryWrapper<T> generate(Class<T> tClass, Object o) {
        QueryWrapper<T> result = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>();

        var fields = new ArrayList<>(Arrays.asList(o.getClass().getDeclaredFields()));

        if (o instanceof SelectQuery) {
            Class<?> superclass = o.getClass().getSuperclass();
            fields.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
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
                case LE:
                    result = result.le(fieldName, param);
                    break;
                case GE:
                    result = result.ge(fieldName, param);
                case IN:
                    if (CollectionUtils.isNotEmpty((Collection<?>) param)) {
                        result = result.in(fieldName, ((Collection<?>) param).toArray());

                    }
                default:
                    break;
            }

        }
        return result;
    }
}
