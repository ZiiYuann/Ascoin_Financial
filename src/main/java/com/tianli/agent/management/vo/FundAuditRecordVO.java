package com.tianli.agent.management.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FundAuditRecordVO {

    private Boolean auditResult;

    private LocalDateTime auditTime;

    private String auditRemark;
}
