package com.tianli.agent.management.bo;

import com.tianli.product.afund.enums.FundReviewStatus;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class FundAuditBO {

    @NotEmpty(message = "交易记录ID不能为空")
    private List<Long> ids;

    @NotNull(message = "审核结果不能为空")
    private FundReviewStatus status;

    private String remark;
}
