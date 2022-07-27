package com.tianli.borrow.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Data
public class BorrowOrderQuery {

    private Long uid;

    private String queryUid;

    private String queryOrderId;

    private BigDecimal minPledgeRate;

    private BigDecimal maxPledgeRate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    private Set<Integer> status;

}
