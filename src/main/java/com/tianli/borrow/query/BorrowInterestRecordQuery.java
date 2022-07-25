package com.tianli.borrow.query;

import lombok.Data;

import java.util.Date;

@Data
public class BorrowInterestRecordQuery {

    private Long orderId;

    private Date startTime;

    private Date endTime;
}
