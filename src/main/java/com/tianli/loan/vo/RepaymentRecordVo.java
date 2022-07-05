package com.tianli.loan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/6/7 10:25
 */
@Data
public class RepaymentRecordVo {
    private Long id;

    /**
     * 还款金额
     */
    private BigDecimal amount;

    private LocalDateTime create_time;

    /**
     * 交易时间
     */
    private String tr_time;

    /**
     * 币类型
     */
    private String token;

    /**
     * 链类型
     */
    private String chain_type;

    /**
     * 偿还本金
     */
    private BigDecimal paid_principal;

    /**
     * 偿还滞纳金
     */
    private BigDecimal paid_forfeit_penalty;

    /**
     * 偿还利息
     */
    private BigDecimal paid_interest;
    /**
     * 剩余本金
     */
    private BigDecimal remaining_principal;
    /**
     * 剩余滞纳金
     */
    private BigDecimal remaining_forfeit_penalty;
    /**
     * 剩余利息
     */
    private BigDecimal remaining_interest;
}
