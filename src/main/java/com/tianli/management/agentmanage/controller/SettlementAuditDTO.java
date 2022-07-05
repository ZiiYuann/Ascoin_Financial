package com.tianli.management.agentmanage.controller;

import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chensong
 * @date 2020-12-28 16:20
 * @since 1.0.0
 */
@Data
public class SettlementAuditDTO {
    @NotNull(message = "主键不能为空")
    private Long id;
    @NotNull(message = "审核状态不能为空")
    private ChargeSettlementStatus status;
    private String review_note;
}
