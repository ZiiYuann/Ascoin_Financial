package com.tianli.charge.enums;


import lombok.Getter;

/**
 * @author:yangkang
 * @create: 2023-03-16 14:37
 * @Description: 操作分类枚举
 */
public enum OperationTypeEnum {

    RECHARGE("recharge", "充值"),
    WITHDRAW("withdraw", "提币"),
    FINANCIAL("financial", "理财"),
    BORROW("borrow", "借贷"),
    EXCHANGE("exchange", "交易所"),
    CHAT("chat", "聊天"),
    ACTIVITY("activity", "活动"),
    GAME("game", "游戏"),
    C2C("c2c","c2c");


    @Getter
    private String nameEn;

    @Getter
    private String name;


    OperationTypeEnum(String nameEn, String name) {
        this.name = name;
        this.nameEn = nameEn;
    }

}
