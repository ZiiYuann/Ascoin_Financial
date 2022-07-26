package com.tianli.borrow.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BorrowInterestRecordQuery {

    @NotNull
    private Long orderId;

    private Date startTime;

    private Date endTime;
}
