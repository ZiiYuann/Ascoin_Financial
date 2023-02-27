package com.tianli.product.aborrow.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.annotation.QueryWrapperGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-27
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBorrowOperationLogQuery {

    @QueryWrapperGenerator(field = "id", op = SqlKeyword.LIKE)
    private Long idStr;

    @QueryWrapperGenerator(field = "id")
    private Long id;

    @QueryWrapperGenerator(field = "uid", op = SqlKeyword.LIKE)
    private Long uidStr;

    @QueryWrapperGenerator(field = "coin")
    private Long coin;

    @QueryWrapperGenerator(field = "charge_type")
    private ChargeType chargeType;

    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.DESC)
    private Boolean createTimeDesc = Boolean.TRUE;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.GE)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.LE)
    private LocalDateTime endTime;
}
