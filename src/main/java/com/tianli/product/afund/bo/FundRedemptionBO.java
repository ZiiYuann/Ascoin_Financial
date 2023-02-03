package com.tianli.product.afund.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FundRedemptionBO {

    @NotNull(message = "订单ID不能为空")
    private Long id;

    @DecimalMin(value = "0.00000001",message = "调整金额必须大于0")
    private BigDecimal redemptionAmount;

}
