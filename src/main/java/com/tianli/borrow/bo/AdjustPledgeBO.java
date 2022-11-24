package com.tianli.borrow.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AdjustPledgeBO {

    @NotNull
    private Long orderId;

    /**
     * 质押类型
     */
    @NotNull(message = "调整类型不能为空")
    private Integer pledgeType;

    /**
     * 调整金额
     */
    @DecimalMin(value = "0.00",message = "调整金额必须大于0")
    private BigDecimal adjustAmount;

    /**
     * 币别
     */
    @NotNull(message = "币别不能为空")
    private String coin;

}
