package com.tianli.management.spot.dto;

import com.tianli.charge.mapper.ChargeStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lzy
 * @date 2022/4/15 5:30 下午
 */
@Data
public class SGWithdrawAuditDTO {
    @NotNull(message = "主键id不能为空")
    private Long id;
    @NotNull(message = "审核状态不能为空")
    private ChargeStatus status;
    private String review_note;

    private String reason;

    private String reason_en;
}
