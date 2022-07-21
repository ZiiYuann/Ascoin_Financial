package com.tianli.common.blockchain;

import lombok.Getter;

public enum CurrencyNetworkType {
    // erc20 以太坊公链代币标准 trc20 波场公链代币标准 bep20 币安链代币标准
    // 以太坊公链代币标准
    erc20("ETH"),
    // 波场公链代币标准
    trc20("TRON"),
    // 币安链代币标准
    bep20("BSC"),
    // okc公链代币标准
    kip20("KIP");
    CurrencyNetworkType(String alias){
        this.alias = alias;
    }

    @Getter
    private String alias;
}
