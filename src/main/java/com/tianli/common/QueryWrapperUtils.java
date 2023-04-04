package com.tianli.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.google.common.base.CaseFormat;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.common.query.SelectQuery;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
public class QueryWrapperUtils {

    private static final String GET = "get";

    private QueryWrapperUtils() {

    }

    @SneakyThrows
    public static <T> QueryWrapper<T> generate(Class<T> tClass, Object o) {
        QueryWrapper<T> result = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        var fields = new ArrayList<>(Arrays.asList(o.getClass().getDeclaredFields()));

        if (o instanceof SelectQuery) {
            Class<?> superclass = o.getClass().getSuperclass();
            fields.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
        for (Field field : fields) {
            String classFieldName = GET + StringUtils.capitalize(field.getName());
            Object param = o.getClass().getMethod(classFieldName).invoke(o);
            QueryWrapperGenerator queryWrapperGenerator = field.getDeclaredAnnotation(QueryWrapperGenerator.class);
            if (Objects.isNull(queryWrapperGenerator) || Objects.isNull(param)
                    || (param instanceof String && StringUtils.isBlank((String) param))) {
                continue;
            }
            SqlKeyword op = queryWrapperGenerator.op();
            String fieldName = "".equals(queryWrapperGenerator.field()) ?
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()) : queryWrapperGenerator.field();
            switch (op) {
                case EQ:
                    result = result.eq(fieldName, param);
                    break;
                case NE:
                    result = result.ne(fieldName, param);
                    break;
                case LIKE:
                    result = result.like(fieldName, param);
                    break;
                case DESC:
                    assert param instanceof Boolean;
                    Boolean desc = (Boolean) param;
                    result = Boolean.TRUE.equals(desc) ? result.orderByDesc(fieldName) : result.orderByAsc(fieldName);
                    break;
                case LE:
                    result = result.le(fieldName, param);
                    break;
                case GE:
                    result = result.ge(fieldName, param);
                    break;
                case IN:
                    assert param instanceof Collection<?>;
                    if (CollectionUtils.isNotEmpty((Collection<?>) param)) {
                        result = result.in(fieldName, ((Collection<?>) param).toArray());
                    }
                    break;
                default:
                    break;
            }

        }
        return result;
    }
}
