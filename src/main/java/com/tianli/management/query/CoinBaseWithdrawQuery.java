package com.tianli.management.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @autoor xianeng
 * @data 2023/4/24 10:34
 */
@Data
public class CoinBaseWithdrawQuery {

    @NotNull(message = "币别名称不允许为空")
    private String name;

    @NotNull(message = "提现小数点位数不允许为空")
    private Integer withdrawDecimals;

    @NotNull(message = "最小提现金额不允许为空")
    private BigDecimal withdrawMin;
}
