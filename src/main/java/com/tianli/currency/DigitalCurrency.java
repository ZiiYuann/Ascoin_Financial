package com.tianli.currency;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.ApplicationContextTool;
import lombok.Data;
import org.javatuples.Pair;

import java.math.BigInteger;

/**
 * @Author wangqiyun
 * @Date 2020/3/12 16:47
 */
@Data
public class DigitalCurrency {
    @JsonSerialize(using = ToStringSerializer.class)
    private final BigInteger amount;
    private final double money;
    private final TokenCurrencyType currencyType;

    public DigitalCurrency(TokenCurrencyType currencyType) {
        this.currencyType = currencyType;
        this.amount = BigInteger.ZERO;
        this.money = 0.0;
    }

    public DigitalCurrency(TokenCurrencyType currencyType, BigInteger amount) {
        this.currencyType = currencyType;
        this.amount = amount;
        this.money = currencyType.money(amount);
    }

    public DigitalCurrency(TokenCurrencyType currencyType, double money) {
        this.currencyType = currencyType;
        this.amount = currencyType.amount(money);
        this.money = money;
    }

    public DigitalCurrency(TokenCurrencyType currencyType, String money) {
        this.currencyType = currencyType;
        this.amount = currencyType.amount(money);
        this.money = currencyType.money(this.amount);
    }

    public DigitalCurrency toOther(TokenCurrencyType currencyType) {
        return toOtherAndPrice(currencyType).getValue0();
    }

    public Pair<DigitalCurrency, Double> toOtherAndPrice(TokenCurrencyType currencyType) {
        if (this.currencyType.equals(currencyType)) return Pair.with(this, 1.0);
        DigitalCurrencyExchangeService digitalCurrencyExchangeService = ApplicationContextTool.getBean(DigitalCurrencyExchangeService.class);
        if (digitalCurrencyExchangeService == null) ErrorCodeEnum.NOT_OPEN.throwException();
        double exchange_rate = digitalCurrencyExchangeService.exchange(this.currencyType, currencyType);
        return Pair.with(new DigitalCurrency(currencyType, this.money * exchange_rate), exchange_rate);
    }


    public static DigitalCurrency instance(TokenCurrencyType currencyType, BigInteger amount, String money) {
        if (currencyType == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (amount == null && money == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (money == null)
            return new DigitalCurrency(currencyType, amount);
        else
            return new DigitalCurrency(currencyType, money);
    }

    public static DigitalCurrency instance(TokenCurrencyType currencyType, BigInteger amount, Double money) {
        return instance(currencyType, amount, money == null ? null : money.toString());
    }
}
