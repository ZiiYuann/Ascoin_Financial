package com.tianli.account.enums;

import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;

/**
 * 余额类型
 */
@Getter
public enum ProductType {
    financial;

    public static ProductType getInstance(String name) {
        ProductType type = EnumUtils.getEnum(ProductType.class, name);
        if (Objects.isNull(type)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        return type;
    }
}
