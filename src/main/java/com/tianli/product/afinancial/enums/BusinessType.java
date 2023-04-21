package com.tianli.product.afinancial.enums;

public enum BusinessType {
    // normal 常规 limited 限时 benefits 新用户福利
    // 常规
    normal("normal","常规"),
    // 限时
    limited("limited","限时"),
    // 新用户福利
    benefits("benefits","新用户福利"),
    share500USDT("share500USDT","瓜分500USDT"),
    FBBANK("FBBANK","FBBank官方");

    BusinessType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
