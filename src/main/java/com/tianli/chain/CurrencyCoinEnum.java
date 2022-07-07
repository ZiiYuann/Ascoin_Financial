package com.tianli.chain;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum CurrencyCoinEnum {
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

    CurrencyCoinEnum() {
        this.name = super.name();
    }

    CurrencyCoinEnum(String name) {
        this.name = name;
    }

    private String name;

    public static CurrencyCoinEnum getCurrencyCoinEnum(String token) {
        for (CurrencyCoinEnum value : CurrencyCoinEnum.values()) {
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
    public static String getSymbols(CurrencyCoinEnum tokenFiat, CurrencyCoinEnum tokenStock) {
        return tokenStock.name.toUpperCase() + tokenFiat.name.toUpperCase();
    }

    public static CurrencyCoinEnum getTokenStock(String symbols) {
        symbols = symbols.toLowerCase();
        return getCurrencyCoinEnum(symbols.replaceAll(CurrencyCoinEnum.usdt.getName(),""));
    }

    public static List<String> getAllUSDTSymbols() {
        List<String> allUSDTSymbols = new ArrayList<>();
        for (CurrencyCoinEnum value : CurrencyCoinEnum.values()) {
            if (value.equals(CurrencyCoinEnum.usdt)) {
                continue;
            }
            allUSDTSymbols.add(getSymbols(CurrencyCoinEnum.usdt, value));
        }
        return allUSDTSymbols;
    }


}
