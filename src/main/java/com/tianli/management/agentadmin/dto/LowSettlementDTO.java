package com.tianli.management.agentadmin.dto;

import com.tianli.dividends.settlement.mapper.LowSettlementChargeType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LowSettlementDTO {

    @NotNull(message = "下级代理商id不能为空")
    private Long id;

    @NotNull(message = "结算金额不能为空")
    private Double amount;

    @NotNull(message = "结算类型不能为空")
    private LowSettlementChargeType type;

    /**
     * 备注信息
     */
    private String note;
}
