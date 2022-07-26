package com.tianli.borrow.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BorrowRepayQuery {

    @NotNull
    private Long orderId;

    private Integer type;

    private Date startTime;

    private Date endTime;
}
