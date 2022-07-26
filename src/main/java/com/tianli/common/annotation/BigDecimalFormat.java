package com.tianli.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tianli.config.decimalformat.BigDecimalDeSerializer;
import com.tianli.config.decimalformat.BigDecimalSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = BigDecimalSerializer.class)
@JsonDeserialize(using = BigDecimalDeSerializer.class)
public @interface BigDecimalFormat {
    // 默认值, 凡是加了 @BigDecimalFormat 注解, 又没有指定 value 值的, 都会被格式化为下面的形式
    String value() default "#0.00";
}
