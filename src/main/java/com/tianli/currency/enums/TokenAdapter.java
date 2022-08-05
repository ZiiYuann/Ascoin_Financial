package com.tianli.currency.enums;

import com.tianli.common.blockchain.CurrencyCoin;
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
    usdt_bep20(CurrencyCoin.usdt, NetworkType.bep20,"0x55d398326f99059ff775485246999027b3197955"),
    usdc_bep20(CurrencyCoin.usdc, NetworkType.bep20,"0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d"),
    // 6位
    usdc_erc20(CurrencyCoin.usdc, NetworkType.erc20,"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
    usdt_erc20(CurrencyCoin.usdt, NetworkType.erc20,"0xdac17f958d2ee523a2206206994597c13d831ec7"),
    // 6位
    usdc_trc20(CurrencyCoin.usdc, NetworkType.trc20,"TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8"),
    usdt_trc20(CurrencyCoin.usdt, NetworkType.trc20,"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"),


    // 主币
    bnb(CurrencyCoin.bnb,NetworkType.bep20,"0x000000"),
    eth(CurrencyCoin.eth,NetworkType.erc20,"0x000000");


    TokenAdapter(CurrencyCoin currencyCoin, NetworkType currencyNetworkType, String contractAddress) {
        this.fiat = false;
        this.network = currencyNetworkType;
        this.currencyCoin = currencyCoin;
        this.contractAddress = contractAddress;
    }

    private final CurrencyCoin currencyCoin;
    private final NetworkType network;
    private final boolean fiat;
    private final String contractAddress;

    /**
     * 如果是主币，调用归集合约传递参数为 new ArrayList
     */
    public List<String> getContractAddressList(){
        if(mainToken(this)){
            return new ArrayList<>();
        }
        return List.of(this.contractAddress);
    }

    /**
     * 是否是主币
     */
    public static boolean mainToken(TokenAdapter tokenAdapter){
        return TokenAdapter.bnb.equals(tokenAdapter) || TokenAdapter.eth.equals(tokenAdapter);
    }

    /**
     * 根据网络和币别获取token包装
     */
    public static TokenAdapter get(CurrencyCoin coin, NetworkType networkType){
        for (TokenAdapter type : TokenAdapter.values()){
            if(type.getCurrencyCoin().equals(coin) && type.getNetwork().equals(networkType)){
                return type;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    /**
     * 根据合约获取代币，主币会抛出异常
     */
    public static TokenAdapter getToken(String contractAddress){
        for (TokenAdapter type : TokenAdapter.values()){
            if(type.getContractAddress().equals(contractAddress)){
                return type;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    /**
     * 根据合约获取主币，代币会抛出异常
     */
    public static TokenAdapter getMainToken(CurrencyCoin coin){
        if(CurrencyCoin.bnb.equals(coin)) return bnb;
        if(CurrencyCoin.eth.equals(coin)) return eth;
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    public double alignment(BigInteger amount) {
        if (amount == null) {
            return 0.0;
        }
        BigDecimal decimal = new BigDecimal(amount);
        switch (this) {
            case usdt_erc20:
            case usdt_trc20:
            case usdc_erc20:
            case usdc_trc20:
                return decimal.divide(new BigDecimal("1000000"),8, RoundingMode.HALF_DOWN).doubleValue();
            case usdt_bep20:
            case usdc_bep20:
            case bnb:
            case eth:
                return decimal.divide(new BigDecimal("1000000000000000000"),8, RoundingMode.HALF_DOWN).doubleValue();
        }
        return 0.0;
    }

    public BigDecimal alignment(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        switch (this) {
            case usdt_erc20:
            case usdt_trc20:
            case usdc_erc20:
            case usdc_trc20:
                return amount.divide(new BigDecimal("1000000"),8, RoundingMode.HALF_DOWN);
            case usdt_bep20:
            case usdc_bep20:
                return amount.divide(new BigDecimal("1000000000000000000"),8, RoundingMode.HALF_DOWN);
        }
        return BigDecimal.ZERO;
    }

    public BigInteger restore(double money) {
        return restore("" + money);
    }

    public BigInteger restore(BigDecimal money) {
        if (money == null) return BigInteger.ZERO;
        switch (this) {
            case usdt_erc20:
            case usdt_trc20:
            case usdc_erc20:
            case usdc_trc20:
                return money.multiply(new BigDecimal("1000000")).toBigInteger();
            case usdt_bep20:
            case usdc_bep20:
                return money.multiply(new BigDecimal("1000000000000000000")).toBigInteger();
        }
        return BigInteger.ZERO;
    }

    public BigInteger restore(String money) {
        BigDecimal decimal = new BigDecimal("" + money);
        return restore(decimal);
    }

}