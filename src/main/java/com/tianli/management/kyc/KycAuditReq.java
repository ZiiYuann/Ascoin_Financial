package com.tianli.management.kyc;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class KycAuditReq {

    @NotNull(message = "审核KYC记录id不能为空")
    private Long id;
    @NotNull(message = "审核KYC状态不能为空")
    private Integer status;
    private String node;
    private String en_node;
}
