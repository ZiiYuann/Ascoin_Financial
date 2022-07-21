package com.tianli.charge.query;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
public class RedeemQuery {

    /**
     * 赎回record 主键
     */
    @NotNull(message = "记录主键不能为null")
    private Long recordId;

    /**
     * 赎回金额
     */
    @NotNull
    @DecimalMin(value = "0.0001", message = "赎回金额不能为空")
    private BigDecimal redeemAmount;

}
