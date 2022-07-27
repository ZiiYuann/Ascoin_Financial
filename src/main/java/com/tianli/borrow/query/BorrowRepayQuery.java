package com.tianli.borrow.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BorrowRepayQuery {

    @NotNull
    private Long orderId;

    private Integer type;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;
}
