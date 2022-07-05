package com.tianli.management.ruleconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialParamConfigDTO {

    /**
     * 门槛设置
     */
    @NotEmpty(message = "门槛设置不能为空")
    private String bet_warning_amount;

    /**
     * 每日奖励 U 数额
     */
    @NotEmpty(message = "每日奖励设置不能为空")
    private String daily_gift_amount;

    /**
     * 新人奖励 U 数额
     */
    @NotEmpty(message = "新人奖励设置不能为空")
    private String new_gift_amount;

    @NotEmpty(message = "kyc认证设置不能为空")
    private String kyc_award;

    private List<CurrencyConfig> currency;
}
