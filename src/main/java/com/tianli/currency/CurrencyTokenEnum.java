package com.tianli.currency;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * 余额币种类型
 */
@Getter
public enum CurrencyTokenEnum {
    usdt_omni(""), // 10位的
    usdt_bep20("0x55d398326f99059ff775485246999027b3197955"), // 18位的
    BF_bep20("0x164d6cbd4576a7ce2a8c0b512b2548a502331edd"), // 18位的
    usdc_bep20("0x8ac76a51cc950d9822d68b83fe1ad97b32cd580d"), // 18位的
    usdt_erc20("0xdac17f958d2ee523a2206206994597c13d831ec7"), // 6位的
    usdc_erc20("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
    usdt_trc20(""), // 6位的
    usdc_trc20(""), // 6位的
    ;

    String address;

    CurrencyTokenEnum(String address) {
        this.address = address;
    }

    public static CurrencyTokenEnum getToken(String address) {
        for (CurrencyTokenEnum value : CurrencyTokenEnum.values()) {
            if (address.equals(value.getAddress())) {
                return value;
            }
        }
        return null;
    }

    public double money(BigInteger remain_BF) {
        if (Objects.isNull(remain_BF)) return 0.0;
        switch (this){
            case usdt_omni:
                return new BigDecimal(remain_BF).movePointLeft(8).doubleValue();
            case usdt_bep20:
            case BF_bep20:
            case usdc_bep20:
                return new BigDecimal(remain_BF).movePointLeft(18).doubleValue();
            case usdt_erc20:
            case usdc_erc20:
                return new BigDecimal(remain_BF).movePointLeft(6).doubleValue();
        }
        return 0.0;
    }

    public BigInteger fromMoney(BigDecimal m) {
        if (Objects.isNull(m)) return BigInteger.ZERO;
        switch (this){
            case usdt_omni:
                return m.movePointRight(8).toBigInteger();
            case usdt_bep20:
            case BF_bep20:
            case usdc_bep20:
                return m.movePointRight(18).toBigInteger();
            case usdt_erc20:
            case usdc_erc20:
                return m.movePointRight(6).toBigInteger();
        }
        return BigInteger.ZERO;
    }

    public String voStr() {
        switch (this){
            case usdt_omni:
            case usdt_bep20:
            case usdt_erc20:
                return "usdt";
            case BF_bep20:
                return "BF";
            case usdc_bep20:
            case usdc_erc20:
                return "usdc";
        }
        return "usdt";
    }
}
