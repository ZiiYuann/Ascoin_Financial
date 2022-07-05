package com.tianli.currency_token.dto;

import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class WithdrawDTO {
    private CurrencyCoinEnum token;
    private CurrencyTypeEnum type;
    private ChainType chain = ChainType.bep20;
    @DecimalMin(value = "0.0001", message = "提现金额过小")
    private BigDecimal amount;
    @NotBlank(message = "接收地址不能为空")
    private String address;
    @NotBlank(message = "手机验证码为空")
    private String code;
}
