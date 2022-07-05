package com.tianli.management.agentmanage.controller;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SaveBalanceAccountDTO  implements Serializable {
    @NotNull(message = "抵扣数额不能为空")
    private Double amount;
    /**
     * 备注信息
     */
    private String note;
}
