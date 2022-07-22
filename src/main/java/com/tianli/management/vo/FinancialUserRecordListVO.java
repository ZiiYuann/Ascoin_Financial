package com.tianli.management.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @since  2022/4/1 7:51 下午
 */
@Data
public class FinancialUserRecordListVO {

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
    /**
     * 存入金额
     */
    private double amount;
    /**
     * 存入时间
     */
    private LocalDateTime depositDate;
    /**
     * 赎回时间
     */
    private LocalDateTime finish_time;
    /**
     * 存入天数
     */
    private Long depositDays;
    /**
     * 剩余天数
     */
    private Long remainingDays;
    /**
     * 可赎回金额
     */
    private double redeemableAmount;
    /**
     * 盈利
     */
    private double profitAmount;


}
