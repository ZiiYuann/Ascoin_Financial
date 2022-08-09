package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/06e76726-793c-413e-b8c0-362d38018567.png"),
    usdc("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/f01d6cd4-d0d5-4330-aeba-f82e1db54c2e.png"),
    bnb("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/37b536ed-a16b-40d5-bc42-8f068e7234d7.png"),
    eth("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/bce61785-ea06-4edd-a7a2-fa7ccf35468b.png");

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
