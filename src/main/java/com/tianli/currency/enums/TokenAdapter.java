package com.tianli.currency.enums;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 代币货币类型
 */
@Getter
public enum TokenAdapter {

    // 代币
    // 18位
    usdt_bep20("usdt", NetworkType.bep20, "0x55d398326f99059ff775485246999027b3197955"),
    usdc_bep20("usdc", NetworkType.bep20, "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d"),
    // 6位
    usdc_erc20("usdc", NetworkType.erc20, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
    usdt_erc20("usdt", NetworkType.erc20, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
    // 6位
    usdc_trc20("usdc", NetworkType.trc20, "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8"),
    usdt_trc20("usdt", NetworkType.trc20, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"),

    // 主币
    bnb("bnb", NetworkType.bep20, "0x000000"),
    eth("eth", NetworkType.erc20, "0x000000"),
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

    public static BigDecimal alignment(BigDecimal amount, int decimals) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.movePointLeft(decimals);
    }

    public static BigInteger restoreBigInteger(BigDecimal money, int decimals) {
        if (money == null) return BigInteger.ZERO;
        return money.movePointRight(decimals).toBigInteger();
    }

    public BigDecimal alignment(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        switch (this) {
            case usdt_erc20:
            case usdt_trc20:
            case usdc_erc20:
            case usdc_trc20:
            case trx:
                return amount.divide(new BigDecimal("1000000"), 8, RoundingMode.DOWN);
            case usdt_bep20:
            case usdc_bep20:
            case bnb:
            case eth:
                return amount.divide(new BigDecimal("1000000000000000000"), 8, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }


}
