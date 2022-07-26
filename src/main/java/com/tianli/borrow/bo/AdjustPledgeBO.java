package com.tianli.borrow.bo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AdjustPledgeBO {

    @NotNull
    private Long orderId;

    private Integer pledgeType;

    @DecimalMin(value = "0.0",message = "调整金额必须大于0")
    private BigDecimal adjustAmount;

    /**
     * 币别
     */
    @NotNull(message = "币别不能为空")
    private CurrencyCoin coin;

}
