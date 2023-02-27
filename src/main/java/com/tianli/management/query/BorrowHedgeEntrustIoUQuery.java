package com.tianli.management.query;

import com.tianli.common.query.IoUQuery;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BorrowHedgeEntrustIoUQuery  extends IoUQuery {

    @NotNull
    private Long brId;

    @NotNull
    private String hedgeCoin;

    // 委托汇率
    @NotNull
    private BigDecimal entrustRate;

}
