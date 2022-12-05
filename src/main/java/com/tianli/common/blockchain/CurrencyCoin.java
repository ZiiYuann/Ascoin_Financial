package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt("https://assure-financial-jp.s3-accelerate.amazonaws.com/file/a4ffebf3-ddf4-4e3f-ae7e-1ab09f808e0f.png","积分"),
    usdc("https://assure-financial-jp.s3-accelerate.amazonaws.com/file/10c3e027-278c-4251-8133-a057eb4ecd1c.png","积分c"),
    bnb("https://assure-financial-jp.s3-accelerate.amazonaws.com/file/49807aaa-e661-4afe-b2d7-2072b157f775.png","积分b"),
    eth("https://assure-financial-jp.s3-accelerate.amazonaws.com/file/5ac3ddbd-7a99-4fd1-8cda-0bc2fcf7e775.png","积分e"),
    trx("","积分t");

    CurrencyCoin(String logoPath,String alias) {
        this.name = super.name();
        this.logoPath = logoPath;
        this.alias = alias;
    }

    private final String name;

    private final String alias;

    private final String logoPath;


    public static List<String> getNameList(){
        List<String> result = new ArrayList<>(CurrencyCoin.values().length);

        for (CurrencyCoin coin : CurrencyCoin.values() ){
            result.add(coin.getName());
        }
        result.remove("trx");
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
