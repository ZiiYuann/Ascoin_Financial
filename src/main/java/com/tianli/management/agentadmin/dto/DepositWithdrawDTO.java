package com.tianli.management.agentadmin.dto;

import com.tianli.currency.TokenCurrencyType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * @author chensong
 * @date 2020-12-24 10:34
 * @since 1.0.0
 */
@Data
public class DepositWithdrawDTO {
    @NotNull(message = "请输入撤回保证金数额")
    @Positive(message = "撤回数额大于0")
    private Double withdrawAmount;
    @NotEmpty(message = "请输入接收地址")
    private String toAddress;
    @Size(max = 300, message = "备注信息最多输入300个字")
    private String note;
    private TokenCurrencyType currencyType;
}
