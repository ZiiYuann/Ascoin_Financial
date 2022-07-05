package com.tianli.management.loan.dto;

import com.tianli.loan.enums.LoanStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/5/26 16:26
 */
@Data
public class LoanAuditDTO {

    @NotNull
    private Long id;

    @NotNull
    private LoanStatusEnum status;
    /**
     *实际借款金额
     */
    @NotNull
    private BigDecimal actual_amount;

    /**
     * 审核原因
     */
    private String reason;

    /**
     * 审核原因英文
     */
    private String reason_en;
}

