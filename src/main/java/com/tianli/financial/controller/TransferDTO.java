package com.tianli.financial.controller;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;

@Data
public class TransferDTO {
    private CurrencyAdaptType from;
    private CurrencyAdaptType to;
    @DecimalMin(value = "0.0001", message = "划转金额不能为空")
    private double amount;
}
