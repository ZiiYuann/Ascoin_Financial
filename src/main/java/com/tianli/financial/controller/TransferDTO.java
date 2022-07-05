package com.tianli.financial.controller;

import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.Data;

import javax.validation.constraints.DecimalMin;

@Data
public class TransferDTO {
    private CurrencyTypeEnum from;
    private CurrencyTypeEnum to;
    @DecimalMin(value = "0.0001", message = "划转金额不能为空")
    private double amount;
    private CurrencyCoinEnum token;
}
