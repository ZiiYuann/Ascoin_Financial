package com.tianli.tool.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by wangqiyun on 2018/1/8.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ClassValidator.class })
@Documented
public @interface ClassValidAnnotation {
    String message() default "参数校验失败";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
