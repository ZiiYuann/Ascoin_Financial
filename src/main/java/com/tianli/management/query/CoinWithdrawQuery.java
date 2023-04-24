package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinWithdrawQuery {

    @NotNull(message = "币别id不允许为空")
    private Long id;

    @NotNull(message = "提现小数点位数不允许为空")
    private Integer withdrawDecimals;

    @NotNull(message = "最小提现金额不允许为空")
    private BigDecimal withdrawMin;

    @NotNull(message = "提现手续费不允许为空")
    private BigDecimal withdrawFixedAmount;

}
