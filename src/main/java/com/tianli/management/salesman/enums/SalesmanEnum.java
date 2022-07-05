package com.tianli.management.salesman.enums;

import lombok.Getter;


/**
 * @author lzy
 * @date 2022/4/7 10:48 下午
 */
@Getter
public enum SalesmanEnum {
    业务员, 业务主管;

    public static boolean isSalesman(String name) {
        for (SalesmanEnum value : SalesmanEnum.values()) {
            if (value.name().equals(name)) return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static SalesmanEnum getSalesmanEnum(String name) {
        for (SalesmanEnum value : SalesmanEnum.values()) {
            if (value.name().equals(name)) return value;
        }
        return null;
    }
}
