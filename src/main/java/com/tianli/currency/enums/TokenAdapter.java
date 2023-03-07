package com.tianli.currency.enums;

import com.tianli.chain.entity.Coin;
import com.tianli.common.blockchain.NetworkType;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 代币货币类型
 */
@Getter
public enum TokenAdapter {

    // 主币
//    btc("btc", NetworkType.btc, "0x000000"),
    bnb("bnb", NetworkType.bep20, "0x000000"),
    eth("eth", NetworkType.erc20, "0x000000"),
    eth_op("eth", NetworkType.erc20_optimistic, "0x000000"),
    eth_arbi("eth", NetworkType.erc20_arbitrum, "0x000000"),
    matic("matic", NetworkType.erc20_polygon, "0x000000"),
    trx(null, NetworkType.trc20, "0x000000");


    TokenAdapter(String currencyCoin, NetworkType currencyNetworkType, String contractAddress) {
        this.fiat = false;
        this.network = currencyNetworkType;
        this.currencyCoin = currencyCoin;
        this.contractAddress = contractAddress;
    }

    private final String currencyCoin;
    private final NetworkType network;
    private final boolean fiat;
    private final String contractAddress;

    public static BigInteger restoreBigInteger(BigDecimal money, int decimals) {
        if (money == null) return BigInteger.ZERO;
        return money.movePointRight(decimals).toBigInteger();
    }

    public static BigDecimal alignment(Coin coin, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.movePointLeft(coin.getDecimals()).setScale(8, RoundingMode.DOWN);
    }

    public static BigDecimal alignment(Coin coin, BigInteger l) {
        BigDecimal amount = new BigDecimal(l);
        if (amount == null) return BigDecimal.ZERO;
        return amount.movePointLeft(coin.getDecimals()).setScale(8, RoundingMode.DOWN);
    }


}
