package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

// 各种币ø
@Getter
public enum CurrencyCoin {
    usdt,
    eth,
    bnb,
    usdc,
    xrp,
    sol,
    ada,
    //luna,
    avax,
    doge,
    dot,
    busd,
    ust,
    matic,
    near,
    ltc,
    atom,
    uni,
    link,
    bch,
    trx,
    ftt,
    xlm,
    etc,
    vet,
    mana,
    fil,
    egld,
    ape,
    sand,
    theta,
    ftm,
    axs,
    xtz,
    rune,
    eos,
    waves,
    aave,
    flow,
    zec,
    cake,
    iota,
    grt,
    cvx,
    one,
    ksm,
    chz,
    gala,
    zil,
    tusd,
    gmt,
    qnt,
    enj,
    lrc,
    bat,
    crv,
    comp,
    usdp,
    hot,
    audio,
    scrt,
    knc,
    iotx,
    yfi,
    skl,
    omg,
    twt,
    paxg,
    //    _1inch("1inch"),
    sxp,
    bnt,
    rndr,
    lpt,
    woo,
    fxs,
    ont,
    storj,
    uma,
    imx,
    sushi,
    btc,
/*------新币-------*/
    stepn
    ;

    CurrencyCoin() {
        this.name = super.name();
    }

    private String name;

    public static CurrencyCoin getCurrencyCoinEnum(String token) {
        for (CurrencyCoin value : CurrencyCoin.values()) {
            if (StrUtil.equals(value.name(), token)) {
                return value;
            }
        }
        return null;
    }

}
