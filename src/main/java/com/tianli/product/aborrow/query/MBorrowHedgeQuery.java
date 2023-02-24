package com.tianli.product.aborrow.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.product.aborrow.enums.HedgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBorrowHedgeQuery {

    @QueryWrapperGenerator(field = "id", op = SqlKeyword.LIKE)
    private Long idStr;

    @QueryWrapperGenerator(field = "bid")
    private Long bid;

    @QueryWrapperGenerator(field = "coin")
    private String coin;

    @QueryWrapperGenerator(field = "hedge_coin")
    private String hedgeCoin;

    @QueryWrapperGenerator(field = "hedge_type")
    private HedgeType hedgeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.GE)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.LE)
    private LocalDateTime endTime;

    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.DESC)
    private Boolean createTimeDesc = Boolean.TRUE;
}
