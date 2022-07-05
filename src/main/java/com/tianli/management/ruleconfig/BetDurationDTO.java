package com.tianli.management.ruleconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BetDurationDTO {

    @NotNull(message = "请输入编号")
    @Range(min = 0, max = 5,message = "编号为1，2，3, 4, 5")
    private Integer id;

    @NotNull(message = "请输入时长")
    private Double duration;

    @NotNull(message = "请输入最小押注金额")
    private Double min_bet_amount;

    private Double max_bet_amount;

    @NotNull(message = "请输入额外收益")
    private Double extra_percentage;

}
