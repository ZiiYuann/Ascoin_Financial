package com.tianli.borrow.query;

import lombok.Data;

import java.util.Date;

@Data
public class BorrowRepayQuery {
    private Long orderId;

    private Integer type;

    private Date startTime;

    private Date endTime;
}
