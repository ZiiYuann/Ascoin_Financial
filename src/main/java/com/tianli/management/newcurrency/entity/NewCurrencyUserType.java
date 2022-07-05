package com.tianli.management.newcurrency.entity;

import lombok.Getter;

@Getter
public enum NewCurrencyUserType {
    kind0("已完成", "0"),
    kind1("待计算", "1"),
    kind2("待投入", "2"),
    kind3("统计中", "3"),

    ;
    String type;
    String code;
    NewCurrencyUserType(String type, String code){
        this.type = type;
        this.code = code;
    }
}