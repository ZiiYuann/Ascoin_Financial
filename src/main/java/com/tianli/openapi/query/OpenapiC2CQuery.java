package com.tianli.openapi.query;

import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author:yangkang
 * @create: 2023-03-30 14:19
 * @Description: c2c冻结参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenapiC2CQuery implements Serializable {

    /**
     * uid
     */
    @NotNull(message = "uid不能为空")
    private Long uid;

    /**
     * 币种
     */
    @NotNull(message = "币种不能为空")
    private String coin;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "类型不能为空")
    private ChargeType chargeType;

    @NotNull(message = "订单号不能为空")
    private Long relatedId;
}
