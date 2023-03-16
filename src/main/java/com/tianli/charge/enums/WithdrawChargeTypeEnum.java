package com.tianli.charge.enums;

import com.tianli.exception.ErrorCodeEnum;

/**
 * @author:yangkang
 * @create: 2023-03-13 18:54
 * @Description: 特殊的交易类型
 */
public enum WithdrawChargeTypeEnum {

    withdraw("withdraw","提币"),
    withdraw_success("reduce","提币成功"),
    withdraw_failed("unfreeze","提币失败"),
    withdraw_freeze("freeze","提币冻结");

    private String type;
    private String description;

    WithdrawChargeTypeEnum(String type,String description) {
        this.type=type;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 根据log_type获取描述
     * @param type
     * @return
     */
    public static  String getDescriptionByType(String type){
        WithdrawChargeTypeEnum[] values = WithdrawChargeTypeEnum.values();
        for (WithdrawChargeTypeEnum value : values) {
            if (value.type.equals(type)){
                return value.description;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    /**
     * 根据描述获取log_type
     * @param name
     * @return
     */
    public static String getTypeByDesc(String name){
        WithdrawChargeTypeEnum[] values = WithdrawChargeTypeEnum.values();
        for (WithdrawChargeTypeEnum value : values) {
            if (value.name().equals(name)){
                return value.type;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }
}
