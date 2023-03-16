package com.tianli.charge.enums;

import com.tianli.exception.ErrorCodeEnum;

/**
 * @author:yangkang
 * @create: 2023-03-15 10:02
 * @Description: 交易类型组枚举
 */
public enum ChargeTypeGroupEnum {

    recharge("充值"),

    withdraw("提币"),

    receive("转入"),
    pay("转出");

    private String typeGroup;

    private ChargeTypeGroupEnum(String typeGroup) {
        this.typeGroup = typeGroup;
    }


    public String getTypeGroup() {
        return typeGroup;
    }

    public void setTypeGroup(String typeGroup) {
        this.typeGroup = typeGroup;
    }

    /**
     * 将类型分组英文转中文名，用于前端展示
     *
     * @param name
     * @return
     */
    public static String getTypeGroup(String name) {
        ChargeTypeGroupEnum[] values = ChargeTypeGroupEnum.values();
        for (ChargeTypeGroupEnum value : values) {
            if (value.name().equals(name)) {
                return value.typeGroup;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    /**
     * 类型分组由中文转英文，用于后台查询
     * @param typeGroup
     * @return
     */
    public static String getName(String typeGroup) {
        ChargeTypeGroupEnum[] values = ChargeTypeGroupEnum.values();
        for (ChargeTypeGroupEnum value : values) {
            if (value.typeGroup.equals(typeGroup)) {
                return value.name();
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

}