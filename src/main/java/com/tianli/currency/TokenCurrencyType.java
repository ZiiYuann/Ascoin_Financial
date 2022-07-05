package com.tianli.currency;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * 代币货币类型
 */
@Getter
public enum TokenCurrencyType {
    usdt_erc20, usdt_trc20, usdt_omni, btc, cny(true), eth, tron, trx, bsc, bnb, usdt_bep20, usdc_bep20, BF_bep20, usdc_erc20, usdc_trc20,;

    public double money(BigInteger amount) {
        if (amount == null) {
            return 0.0;
        }
        BigDecimal decimal = new BigDecimal(amount);
        switch (this) {
            case usdt_erc20:
            case trx:
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
        return _money(amount).toString();
    }

    public String amountStr2MoneyStr(String amount) {
        if (StringUtils.isBlank(amount)) {
            return "0";
        }
        BigInteger bigInteger = new BigInteger(amount);
        return money2String(bigInteger);
    }

    public BigDecimal _money(BigInteger amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal decimal = new BigDecimal(amount);
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

    public String moneyStr2AmountStr(String money) {
        if (StringUtils.isBlank(money)) {
            return "0";
        }
        return amount(money).toString();
    }

    public static TokenCurrencyType getTokenCurrencyType(String type) {
        if (StrUtil.isBlank(type)) {
            return null;
        }
        return Arrays.stream(TokenCurrencyType.values()).filter(x -> x.name().equals(type)).findFirst().orElse(null);
    }

    TokenCurrencyType() {
        this.fiat = false;
    }

    TokenCurrencyType(boolean fiat) {
        this.fiat = fiat;
    }

    private final boolean fiat;
}
