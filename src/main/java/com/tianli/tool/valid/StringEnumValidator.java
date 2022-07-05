package com.tianli.tool.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by wangqiyun on 2018/7/11.
 */
public class StringEnumValidator implements ConstraintValidator<StringEnum, String> {

    private String[] stringEnum;
    private Class<? extends Enum> e;

    @Override
    public void initialize(StringEnum constraintAnnotation) {
        stringEnum = constraintAnnotation.value();
        e = constraintAnnotation.ENUM();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) return true;
        if (!e.equals(Enum.class)) {
            try {
                Enum of = Enum.valueOf(e, s);
                return true;
            } catch (IllegalArgumentException e) {}
        }
        for (String str : stringEnum) {
            if (s.equals(str))
                return true;
        }
        return false;
    }
}
