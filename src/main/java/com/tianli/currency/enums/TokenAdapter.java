package com.tianli.currency.enums;

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
    // 6位
    usdc_erc20_op("usdc", NetworkType.erc20_op, "0x7F5c764cBc14f9669B88837ca1490cCa17c31607"),
    usdt_erc20_op("usdt", NetworkType.erc20_op, "0x94b008aA00579c1307B0EF2c499aD98a8ce58e58"),
    // 6位
    usdc_erc20_arbi("usdc", NetworkType.erc20_arbi, "0xFF970A61A04b1cA14834A43f5dE4533eBDDB5CC8"),
    usdt_erc20_arbi("usdt", NetworkType.erc20_arbi, "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9"),
    // 6位
    usdc_erc20_polygon("usdc", NetworkType.erc20_polygon, "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174"),
    usdt_erc20_polygon("usdt", NetworkType.erc20_polygon, "0xc2132D05D31c914a87C6611C10748AEb04B58e8F"),


    // 主币
    btc("btc", NetworkType.btc, "0x000000"),
    bnb("bnb", NetworkType.bep20, "0x000000"),
    eth("eth", NetworkType.erc20, "0x000000"),
    eth_op("eth", NetworkType.erc20_op, "0x000000"),
    eth_arbi("eth", NetworkType.erc20_arbi, "0x000000"),
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
            case btc:
                return amount.divide(new BigDecimal("100000000"), 8, RoundingMode.DOWN);
            case usdt_erc20:
            case usdt_trc20:
            case usdt_erc20_op:
            case usdt_erc20_arbi:
            case usdt_erc20_polygon:
            case usdc_erc20:
            case usdc_trc20:
            case usdc_erc20_op:
            case usdc_erc20_arbi:
            case usdc_erc20_polygon:
            case trx:
                return amount.divide(new BigDecimal("1000000"), 8, RoundingMode.DOWN);
            case usdt_bep20:
            case usdc_bep20:
            case bnb:
            case eth:
            case matic:
            case eth_op:
            case eth_arbi:
                return amount.divide(new BigDecimal("1000000000000000000"), 8, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }


}
