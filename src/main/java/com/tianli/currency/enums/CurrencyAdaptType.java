package com.tianli.currency.enums;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 代币货币类型
 */
@Getter
public enum CurrencyAdaptType {

    // 18位
    usdt_bep20(CurrencyCoin.usdt, NetworkType.bep20,"0x55d398326f99059ff775485246999027b3197955"),
    usdc_bep20(CurrencyCoin.usdc, NetworkType.bep20,"0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d"),
    // 6位
    usdc_erc20(CurrencyCoin.usdc, NetworkType.erc20,"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
    usdt_erc20(CurrencyCoin.usdt, NetworkType.erc20,"0xdac17f958d2ee523a2206206994597c13d831ec7"),
    // 6位
    usdc_trc20(CurrencyCoin.usdc, NetworkType.trc20,"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"),
    usdt_trc20(CurrencyCoin.usdt, NetworkType.trc20,"TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8");

    CurrencyAdaptType(CurrencyCoin currencyCoin) {
        this.fiat = false;
        this.currencyCoin = currencyCoin;
        this.network = null;
        this.contractAddress = null;
    }

    CurrencyAdaptType(CurrencyCoin currencyCoin, NetworkType currencyNetworkType,String contractAddress) {
        this.fiat = false;
        this.network = currencyNetworkType;
        this.currencyCoin = currencyCoin;
        this.contractAddress = contractAddress;
    }

    private final CurrencyCoin currencyCoin;
    private final NetworkType network;
    private final boolean fiat;
    private final String contractAddress;

    public static CurrencyAdaptType get(CurrencyCoin coin, NetworkType networkType){
        for (CurrencyAdaptType type : CurrencyAdaptType.values()){
            if(type.getCurrencyCoin().equals(coin) && type.getNetwork().equals(networkType)){
                return type;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    public static CurrencyAdaptType get(String contractAddress){
        for (CurrencyAdaptType type : CurrencyAdaptType.values()){
            if(type.getContractAddress().equals(contractAddress)){
                return type;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }


    public double money(BigInteger amount) {
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
                return decimal.divide(new BigDecimal("1000000000000000000"),8, RoundingMode.HALF_DOWN).doubleValue();
        }
        return 0.0;
    }

    public BigDecimal moneyBigDecimal(BigInteger amount) {
       return moneyBigDecimal(new BigDecimal(amount));
    }

    public BigDecimal moneyBigDecimal(BigDecimal amount) {
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

    public BigInteger amount(double money) {
        return amount("" + money);
    }

    public BigInteger amount(BigDecimal money) {
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

    public BigInteger amount(String money) {
        BigDecimal decimal = new BigDecimal("" + money);
        return amount(decimal);
    }

}
