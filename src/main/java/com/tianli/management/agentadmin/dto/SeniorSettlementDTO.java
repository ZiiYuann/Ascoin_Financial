package com.tianli.management.agentadmin.dto;

import com.tianli.currency.TokenCurrencyType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SeniorSettlementDTO {
    @NotNull(message = "币种类型不能为空")
    private TokenCurrencyType currencyType;
    @NotNull(message = "结算数额不能为空")
    @Positive(message = "结算数额大于0")
    private Double amount;
    @NotBlank(message = "请输入接收地址")
    private String toAddress;
    private String note;
}
