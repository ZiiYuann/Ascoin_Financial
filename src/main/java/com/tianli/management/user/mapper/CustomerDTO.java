package com.tianli.management.user.mapper;

import com.tianli.user.mapper.UserStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String phone;
    private String nick;
    private UserStatus status;
    private LocalDateTime create_time;
    /**
     * eth / usdt-erc20
     */
    private String eth;
    /**
     * btc / usdt-omni
     */
    private String btc;

    private String trc20;

    private String bsc;

    private BigInteger balance;

    private BigInteger balance_BF;
    /**
     * 优惠余额
     */
    private BigInteger weak_balance;

    private String facebook;

    private String line;

    private Boolean use_robot;

    private Integer auto_count;

    private BigDecimal auto_amount;

    /**
     * 间隔时间
     */
    private String interval_time;

    /**
     * 胜率
     */
    private Double win_rate;

    /**
     * 利润率
     */
    private Double profit_rate;

    /**
     * 备注
     */
    private String node;
    private Integer credit_score;
    private String adjust_reason;
    private Integer user_type;

    private String salesman_username;

    /**
     * 充值金额
     */
    private BigDecimal recharge_amount;
    /**
     * 提现金额
     */
    private BigDecimal withdrawal_amount;
    /**
     * 利润
     */
    private BigDecimal profit;
}
