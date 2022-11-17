package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundIncomeTestQuery {

    private Long uid;

    @NotNull(message = "持有记录id不允许为空")
    private Long recordId;

    @NotNull(message = "记算利息时间不允许为空")
    private LocalDateTime now;

    @NotNull(message = "订单申购时间不允许为空")
    private LocalDateTime createTime;
}

