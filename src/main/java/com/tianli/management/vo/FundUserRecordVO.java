package com.tianli.management.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundUserRecordVO {

    private Long Uid;

    private BigDecimal holdAmount;

    private BigDecimal interestAmount;

    private BigDecimal payInterestAmount;

    private BigDecimal waitPayInterestAmount;

}
