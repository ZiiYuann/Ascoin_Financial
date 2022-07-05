package com.tianli.management.agentmanage.controller;

import com.tianli.deposit.mapper.ChargeDepositStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chensong
 * @date 2020-12-28 15:51
 * @since 1.0.0
 */
@Data
public class DepositAuditDTO {
    @NotNull(message = "主键不能为空")
    private Long id;
    @NotNull(message = "审核状态不能为空")
    private ChargeDepositStatus status;
    private String review_note;
}
