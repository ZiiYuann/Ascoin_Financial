package com.tianli.product.aborrow.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.product.aborrow.enums.PledgeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowUserQuery {

    @QueryWrapperGenerator(field = "borrow_coins", op = SqlKeyword.LIKE)
    private String borrowCoinStr;

    @QueryWrapperGenerator(field = "pledge_coins", op = SqlKeyword.LIKE)
    private String pledgeCoinStr;

    @QueryWrapperGenerator(field = "uid")
    private String uid;

    @QueryWrapperGenerator(field = "currency_pledge_rate", op = SqlKeyword.LE)
    private BigDecimal pledgeRateMax;

    @QueryWrapperGenerator(field = "currency_pledge_rate", op = SqlKeyword.GE)
    private BigDecimal pledgeRateMin;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.GE)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(field = "create_time", op = SqlKeyword.LE)
    private LocalDateTime endTime;

    @QueryWrapperGenerator(field = "finish")
    private Boolean finish;

    @QueryWrapperGenerator(field = "pledge_status")
    private PledgeStatus pledgeStatus;


}
