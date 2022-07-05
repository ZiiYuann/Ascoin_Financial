package com.tianli.tool.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by wangqiyun on 2018/7/11.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {StringEnumValidator.class})
@Documented
public @interface StringEnum {
    String message() default "参数校验失败";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] value() default {};

    Class<? extends Enum> ENUM() default Enum.class;
}
