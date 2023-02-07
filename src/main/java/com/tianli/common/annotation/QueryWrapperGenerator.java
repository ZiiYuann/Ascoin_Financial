package com.tianli.common.annotation;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryWrapperGenerator {

    SqlKeyword op() default SqlKeyword.EQ;

    String field();
}
