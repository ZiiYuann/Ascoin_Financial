package com.tianli.charge.enums;

/**
 * @author:yangkang
 * @create: 2023-03-10 17:16
 * @Description: 可见类型，目前分普通用户、商家、代理
 */
public enum VisibleTypeEnum {
    //todo 商家暂时跟用户一样

    normal(1,"普通用户"),
    agent(0,"代理");

    private Integer symbol;

    private String description;

    VisibleTypeEnum(Integer symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public Integer getSymbol() {
        return symbol;
    }

    public void setSymbol(Integer symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
