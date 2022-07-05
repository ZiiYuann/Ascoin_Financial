package com.tianli.management.financial.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/1 7:10 下午
 */
@Data
public class FinancialUserRecordListDto {


    /**
     * id
     */
    private String id;
    /**
     * 邮箱
     */
    private String username;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 理财类型
     */
    private String financialProductName;

    private double rate;
    /**
     * 存入金额
     */
    private BigInteger amount;
    /**
     * 存入时间
     */
    private LocalDateTime depositDate;
    /**
     * 开始时间
     */
    private LocalDate start_date;
    /**
     * 结束时间
     */
    private LocalDate end_date;
    /**
     * 赎回时间
     */
    private LocalDateTime finish_time;

    private String financial_product_type;

    private String status;

}
