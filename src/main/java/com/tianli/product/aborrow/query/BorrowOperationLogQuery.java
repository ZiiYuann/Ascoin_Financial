package com.tianli.product.aborrow.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.common.query.SelectQuery;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowOperationLogQuery extends SelectQuery {

    @QueryWrapperGenerator(field = "charge_type")
    private ChargeType chargeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.GE)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.LE)
    private LocalDateTime endTime;

    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.DESC)
    private Boolean createTimeDesc = Boolean.TRUE;

}
