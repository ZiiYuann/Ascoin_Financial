package com.tianli.currency.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.service.DigitalCurrencyExchangeService;
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
    private final TokenAdapter tokenAdapter;

    public DigitalCurrency(TokenAdapter tokenAdapter) {
        this.tokenAdapter = tokenAdapter;
        this.amount = BigInteger.ZERO;
        this.money = 0.0;
    }

    public DigitalCurrency(TokenAdapter tokenAdapter, BigInteger amount) {
        this.tokenAdapter = tokenAdapter;
        this.amount = amount;
        this.money = tokenAdapter.alignment(amount);
    }

    public DigitalCurrency(TokenAdapter tokenAdapter, double money) {
        this.tokenAdapter = tokenAdapter;
        this.amount = tokenAdapter.restore(money);
        this.money = money;
    }

    public DigitalCurrency(TokenAdapter tokenAdapter, String money) {
        this.tokenAdapter = tokenAdapter;
        this.amount = tokenAdapter.restore(money);
        this.money = tokenAdapter.alignment(this.amount);
    }

    public DigitalCurrency toOther(TokenAdapter tokenAdapter) {
        return toOtherAndPrice(tokenAdapter).getValue0();
    }

    public Pair<DigitalCurrency, Double> toOtherAndPrice(TokenAdapter tokenAdapter) {
        if (this.tokenAdapter.equals(tokenAdapter)) return Pair.with(this, 1.0);
        DigitalCurrencyExchangeService digitalCurrencyExchangeService = ApplicationContextTool.getBean(DigitalCurrencyExchangeService.class);
        if (digitalCurrencyExchangeService == null) ErrorCodeEnum.NOT_OPEN.throwException();
        double exchange_rate = digitalCurrencyExchangeService.exchange(this.tokenAdapter, tokenAdapter);
        return Pair.with(new DigitalCurrency(tokenAdapter, this.money * exchange_rate), exchange_rate);
    }


    public static DigitalCurrency instance(TokenAdapter tokenAdapter, BigInteger amount, String money) {
        if (tokenAdapter == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (amount == null && money == null) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        if (money == null)
            return new DigitalCurrency(tokenAdapter, amount);
        else
            return new DigitalCurrency(tokenAdapter, money);
    }

    public static DigitalCurrency instance(TokenAdapter tokenAdapter, BigInteger amount, Double money) {
        return instance(tokenAdapter, amount, money == null ? null : money.toString());
    }
}
