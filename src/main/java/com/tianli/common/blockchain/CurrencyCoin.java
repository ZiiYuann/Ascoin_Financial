package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt("https://financial-dev-jp.s3-accelerate.amazonaws.com/file/66e1ea07-7da8-459b-8d50-f49f1e36cd5e.png"),
    usdc("https://financial-dev-jp.s3-accelerate.amazonaws.com/file/0887ce3c-4699-4d40-8992-5acb5e5b760c.png"),
    bnb("https://financial-dev-jp.s3-accelerate.amazonaws.com/file/eb26dd33-9638-45eb-9501-6ec5771954eb.png"),
    eth("https://financial-dev-jp.s3-accelerate.amazonaws.com/file/31cf3184-ed54-4872-803a-092a894b7ad8.png");

    CurrencyCoin(String logoPath) {
        this.name = super.name();
        this.logoPath = logoPath;
    }

    private final String name;

    private final String logoPath;


    public static List<String> getNameList(){
        List<String> result = new ArrayList<>(CurrencyCoin.values().length);

        for (CurrencyCoin coin : CurrencyCoin.values() ){
            result.add(coin.getName());
        }
        return result;
    }


    public static CurrencyCoin getCurrencyCoinEnum(String token) {
        for (CurrencyCoin value : CurrencyCoin.values()) {
            if (StrUtil.equals(value.name(), token)) {
                return value;
            }
        }
        return null;
    }

}
