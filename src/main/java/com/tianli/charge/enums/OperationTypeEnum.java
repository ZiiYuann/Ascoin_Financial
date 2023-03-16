package com.tianli.charge.enums;


/**
 * @author:yangkang
 * @create: 2023-03-16 14:37
 * @Description: 操作分类枚举
 */
public enum OperationTypeEnum {

    RECHARGE("recharge","充值"),
    WITHDRAW("withdraw","提币"),
    FINANCIAL("financial","理财"),
    BORROW("borrow","借贷"),
    EXCHANGE("exchange","交易所"),
    CHAT("chat","聊天"),
    ACTIVITY("activity","活动"),
    GAME("game","游戏");


    private String enName;

    private String name;


    private OperationTypeEnum(String enName,String name) {
        this.name = name;
        this.enName=enName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }
}
