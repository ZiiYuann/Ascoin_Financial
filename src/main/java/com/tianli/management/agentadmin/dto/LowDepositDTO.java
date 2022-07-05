package com.tianli.management.agentadmin.dto;

import com.tianli.deposit.mapper.LowDepositChargeType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LowDepositDTO {

    @NotNull(message = "下级代理商id不能为空")
    private Long id;

    @NotNull(message = "保证金数额不能为空")
    private Double amount;

    @NotNull(message = "类型不能为空")
    private LowDepositChargeType type;

    /**
     * 备注信息
     */
    private String note;
}
