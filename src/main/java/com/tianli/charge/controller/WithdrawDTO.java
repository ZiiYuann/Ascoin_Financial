package com.tianli.charge.controller;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 15:20
 */
@Data
public class WithdrawDTO {
    @NotNull(message = "币种不能为空")
    private CurrencyAdaptType currencyAdaptType;
    private CurrencyTokenEnum token;
    @DecimalMin(value = "0.0001", message = "提现金额不能为空")
    private double amount;
    @NotBlank(message = "接收地址不能为空")
    private String address;
    @NotBlank(message = "手机验证码为空")
    private String code;
}
