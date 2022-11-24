package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/923a67de-3b57-442f-bb15-88f19fa160ed.png","积分"),
    usdc("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/f01d6cd4-d0d5-4330-aeba-f82e1db54c2e.png","积分c"),
    bnb("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/37b536ed-a16b-40d5-bc42-8f068e7234d7.png","积分b"),
    eth("https://twallet-news-jp.oss-accelerate.aliyuncs.com/file/bce61785-ea06-4edd-a7a2-fa7ccf35468b.png","积分e"),
    trx("","积分t");

    CurrencyCoin(String logoPath,String alias) {
        this.name = super.name();
        this.logoPath = logoPath;
        this.alias = alias;
    }

    private final String name;

    private final String alias;

    private final String logoPath;





}
