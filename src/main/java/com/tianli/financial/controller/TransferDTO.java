package com.tianli.financial.controller;

import com.tianli.common.blockchain.CurrencyCoinEnum;
import com.tianli.currency.CurrencyTypeEnum;
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
