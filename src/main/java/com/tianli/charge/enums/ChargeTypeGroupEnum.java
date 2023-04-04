package com.tianli.charge.enums;

import com.tianli.exception.ErrorCodeEnum;

/**
 * @author:yangkang
 * @create: 2023-03-15 10:02
 * @Description: 交易类型组枚举
 */
public enum ChargeTypeGroupEnum {

    recharge( "Recharge","充值",1),

    withdraw("Withdraw","提币",2),

    in("Transfer in","转入",3),

    out("Transfer out","转出",4);

    private String typeGroupEn;
    private String typeGroup;
    private Integer order;

    private ChargeTypeGroupEnum(String typeGroupEn,String typeGroup,Integer order) {
        this.typeGroupEn=typeGroupEn;
        this.typeGroup = typeGroup;
        this.order=order;
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
            if (value.getTypeGroupEn().equals(name)) {
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

    public static Integer getOrder(String name){
        ChargeTypeGroupEnum[] values = ChargeTypeGroupEnum.values();
        for (ChargeTypeGroupEnum value : values) {
            if (value.getTypeGroupEn().equals(name)) {
                return value.getOrder();
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getTypeGroupEn() {
        return typeGroupEn;
    }

    public void setTypeGroupEn(String typeGroupEn) {
        this.typeGroupEn = typeGroupEn;
    }
}
