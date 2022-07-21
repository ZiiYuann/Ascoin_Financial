package com.tianli.currency.enums;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.ErrorCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 代币货币类型
 */
@Getter
public enum CurrencyAdaptType {

    usdt_bep20(CurrencyCoin.usdt, NetworkType.bep20),
    usdc_erc20(CurrencyCoin.usdc, NetworkType.erc20),
    usdc_trc20(CurrencyCoin.usdc, NetworkType.trc20),
    usdc_bep20(CurrencyCoin.usdc, NetworkType.bep20),

    usdt_erc20(CurrencyCoin.usdt, NetworkType.erc20),
    eth(CurrencyCoin.eth, NetworkType.erc20),

    usdt_trc20(CurrencyCoin.usdt, NetworkType.trc20),
    tron,

    usdt_omni(CurrencyCoin.usdt),
    btc,

    cny(true),
    bsc,
    bnb(CurrencyCoin.bnb, NetworkType.bep20),
    BF_bep20;

    CurrencyAdaptType(CurrencyCoin currencyCoin) {
        this.fiat = false;
        this.currencyCoin = currencyCoin;
    }

    CurrencyAdaptType(CurrencyCoin currencyCoin, NetworkType currencyNetworkType) {
        this.fiat = false;
        this.currencyNetworkType = currencyNetworkType;
        this.currencyCoin = currencyCoin;
    }

    CurrencyAdaptType(boolean fiat) {
        this.fiat = fiat;
    }

    CurrencyAdaptType(){
        this.fiat = false;
    }

    private CurrencyCoin currencyCoin;
    private NetworkType currencyNetworkType;
    private final boolean fiat;

    public static CurrencyAdaptType get(CurrencyCoin coin, NetworkType networkType){
        for (CurrencyAdaptType type : CurrencyAdaptType.values()){
            if(type.getCurrencyCoin().equals(coin) && type.getCurrencyNetworkType().equals(networkType)){
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
                return decimal.divide(new BigDecimal("1000000")).doubleValue();
            case usdt_omni:
            case btc:
                return decimal.divide(new BigDecimal("100000000")).doubleValue();
            case cny:
                return decimal.divide(new BigDecimal("100")).doubleValue();
            case eth:
            case bnb:
            case usdt_bep20:
            case BF_bep20:
            case usdc_bep20:
                return decimal.divide(new BigDecimal("1000000000000000000")).doubleValue();
        }
        return 0.0;
    }

    public String money2String(BigInteger amount) {
        return moneyBigDecimal(amount).toString();
    }

    public String amountStr2MoneyStr(String amount) {
        if (StringUtils.isBlank(amount)) {
            return "0";
        }
        BigInteger bigInteger = new BigInteger(amount);
        return money2String(bigInteger);
    }

    public BigDecimal moneyBigDecimal(BigInteger amount) {
       return moneyBigDecimal(new BigDecimal(amount));
    }

    public BigDecimal moneyBigDecimal(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal decimal = amount;
        switch (this) {
            case usdt_erc20:
            case usdt_trc20:
            case usdc_erc20:
            case usdc_trc20:
                return decimal.divide(new BigDecimal("1000000"));
            case usdt_omni:
            case btc:
                return decimal.divide(new BigDecimal("100000000"));
            case cny:
                return decimal.divide(new BigDecimal("100"));
            case eth:
            case usdt_bep20:
            case BF_bep20:
            case bnb:
            case usdc_bep20:
                return decimal.divide(new BigDecimal("1000000000000000000"));
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
            case usdt_omni:
            case btc:
                return money.multiply(new BigDecimal("100000000")).toBigInteger();
            case cny:
                return money.multiply(new BigDecimal("100")).toBigInteger();
            case eth:
            case BF_bep20:
            case usdt_bep20:
            case bnb:
            case usdc_bep20:
                return money.multiply(new BigDecimal("1000000000000000000")).toBigInteger();
        }
        return BigInteger.ZERO;
    }

    public BigInteger amount(String money) {
        BigDecimal decimal = new BigDecimal("" + money);
        return amount(decimal);
    }

    public static List<CurrencyAdaptType> listByCoin(CurrencyCoin coin){
        List<CurrencyAdaptType> currencyAdaptTypes = new ArrayList<>();
        for (CurrencyAdaptType currencyAdaptType: CurrencyAdaptType.values()){
            if(currencyAdaptType.getCurrencyCoin().equals(coin)){
                currencyAdaptTypes.add(currencyAdaptType);
            }
        }
        return currencyAdaptTypes;
    }
}
