package com.tianli.currency;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.currency.enums.CurrencyAdaptType;
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
    private final CurrencyAdaptType currencyAdaptType;

    public DigitalCurrency(CurrencyAdaptType currencyAdaptType) {
        this.currencyAdaptType = currencyAdaptType;
        this.amount = BigInteger.ZERO;
        this.money = 0.0;
    }

    public DigitalCurrency(CurrencyAdaptType currencyAdaptType, BigInteger amount) {
        this.currencyAdaptType = currencyAdaptType;
        this.amount = amount;
        this.money = currencyAdaptType.money(amount);
    }

    public DigitalCurrency(CurrencyAdaptType currencyAdaptType, double money) {
        this.currencyAdaptType = currencyAdaptType;
        this.amount = currencyAdaptType.amount(money);
        this.money = money;
    }

    public DigitalCurrency(CurrencyAdaptType currencyAdaptType, String money) {
        this.currencyAdaptType = currencyAdaptType;
        this.amount = currencyAdaptType.amount(money);
        this.money = currencyAdaptType.money(this.amount);
    }

    public DigitalCurrency toOther(CurrencyAdaptType currencyAdaptType) {
        return toOtherAndPrice(currencyAdaptType).getValue0();
    }

    public Pair<DigitalCurrency, Double> toOtherAndPrice(CurrencyAdaptType currencyAdaptType) {
        if (this.currencyAdaptType.equals(currencyAdaptType)) return Pair.with(this, 1.0);
        DigitalCurrencyExchangeService digitalCurrencyExchangeService = ApplicationContextTool.getBean(DigitalCurrencyExchangeService.class);
        if (digitalCurrencyExchangeService == null) ErrorCodeEnum.NOT_OPEN.throwException();
        double exchange_rate = digitalCurrencyExchangeService.exchange(this.currencyAdaptType, currencyAdaptType);
        return Pair.with(new DigitalCurrency(currencyAdaptType, this.money * exchange_rate), exchange_rate);
    }


    public static DigitalCurrency instance(CurrencyAdaptType currencyAdaptType, BigInteger amount, String money) {
        if (currencyAdaptType == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (amount == null && money == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (money == null)
            return new DigitalCurrency(currencyAdaptType, amount);
        else
            return new DigitalCurrency(currencyAdaptType, money);
    }

    public static DigitalCurrency instance(CurrencyAdaptType currencyAdaptType, BigInteger amount, Double money) {
        return instance(currencyAdaptType, amount, money == null ? null : money.toString());
    }
}
