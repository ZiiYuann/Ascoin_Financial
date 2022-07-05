package com.tianli.tool.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by wangqiyun on 2018/1/8.
 */
public class ClassValidator implements ConstraintValidator<ClassValidAnnotation, ClassValid> {
    @Override
    public boolean isValid(ClassValid classValid, ConstraintValidatorContext constraintValidatorContext) {
        return classValid == null || classValid.valid();
    }
}
