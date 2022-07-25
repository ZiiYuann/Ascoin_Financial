package com.tianli.borrow.query;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Data
public class BorrowOrderQuery {

    private Long uid;

    private String queryUid;

    private String queryOrderId;

    private BigDecimal minPledgeRate;

    private BigDecimal maxPledgeRate;

    private Date startTime;

    private Date endTime;

    private Set<Integer> status;

}
