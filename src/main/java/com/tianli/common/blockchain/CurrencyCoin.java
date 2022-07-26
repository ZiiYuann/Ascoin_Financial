package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt("https://wallet-news-dev.oss-cn-shanghai.aliyuncs.com/file/f736f08c-0b0c-4cd0-8beb-1b6445462b80.png"),
    usdc("https://wallet-news-dev.oss-cn-shanghai.aliyuncs.com/file/e17e613e-5fbd-4317-8b2b-b2ba1858d9c5.png")
//    eth,
//    bnb,
//    usdc,
//    xrp,
//    sol,
//    ada,
//    //luna,
//    avax,
//    doge,
//    dot,
//    busd,
//    ust,
//    matic,
//    near,
//    ltc,
//    atom,
//    uni,
//    link,
//    bch,
//    trx,
//    ftt,
//    xlm,
//    etc,
//    vet,
//    mana,
//    fil,
//    egld,
//    ape,
//    sand,
//    theta,
//    ftm,
//    axs,
//    xtz,
//    rune,
//    eos,
//    waves,
//    aave,
//    flow,
//    zec,
//    cake,
//    iota,
//    grt,
//    cvx,
//    one,
//    ksm,
//    chz,
//    gala,
//    zil,
//    tusd,
//    gmt,
//    qnt,
//    enj,
//    lrc,
//    bat,
//    crv,
//    comp,
//    usdp,
//    hot,
//    audio,
//    scrt,
//    knc,
//    iotx,
//    yfi,
//    skl,
//    omg,
//    twt,
//    paxg,
//    //    _1inch("1inch"),
//    sxp,
//    bnt,
//    rndr,
//    lpt,
//    woo,
//    fxs,
//    ont,
//    storj,
//    uma,
//    imx,
//    sushi,
//    btc,
///*------新币-------*/
//    stepn
    ;

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
