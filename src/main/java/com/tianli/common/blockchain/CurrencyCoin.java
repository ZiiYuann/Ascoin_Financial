package com.tianli.common.blockchain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// 各种币
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

    CurrencyCoin(String name) {
        this.name = name;
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

    /**
     * @param tokenFiat  法币
     * @param tokenStock 货币
     * @return
     */
    public static String getSymbols(CurrencyCoin tokenFiat, CurrencyCoin tokenStock) {
        return tokenStock.name.toUpperCase() + tokenFiat.name.toUpperCase();
    }

    public static CurrencyCoin getTokenStock(String symbols) {
        symbols = symbols.toLowerCase();
        return getCurrencyCoinEnum(symbols.replaceAll(CurrencyCoin.usdt.getName(),""));
    }

    public static List<String> getAllUSDTSymbols() {
        List<String> allUSDTSymbols = new ArrayList<>();
        for (CurrencyCoin value : CurrencyCoin.values()) {
            if (value.equals(CurrencyCoin.usdt)) {
                continue;
            }
            allUSDTSymbols.add(getSymbols(CurrencyCoin.usdt, value));
        }
        return allUSDTSymbols;
    }


}
