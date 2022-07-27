package com.tianli.borrow.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BorrowPledgeRecordQuery {

    /**
     * 订单ID
     */
    @NotNull
    private Long orderId;

    /**
     * 订单类型
     */
    private Integer type;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;
}
