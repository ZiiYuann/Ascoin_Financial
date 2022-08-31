package com.tianli.currency.enums;

import lombok.Getter;

@Getter
public enum NationalCurrencyEnum {
    CNY("人民币", 1),
    USD("美元", 2),
    EUR("欧元", 14),
    IDR("印尼盾", 16),
    VND("越南盾", 5),
    THB("泰国", 31),
    MYR("马来西亚林吉特", 22),
    SGD("新加坡元", 3),
    HKD("港币",  13),
    TWD("新台币", 10),
    JPY("日元", -1),
    KRW("韩元", -1),
    AUD("澳大利亚元", 7),
    CAD("加拿大元", 6),
    GBP("英镑", 12)

    ;
    String des;
    int currency;
    NationalCurrencyEnum(String des, int currency){
        this.des = des;
        this.currency = currency;
    }
}
