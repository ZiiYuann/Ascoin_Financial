package com.tianli.management.spot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/24 15:38
 */
@Data
public class SGRechargeListVo {
    /**
     * id
     */
    private Long id;
    /**
     * 用户邮箱
     */
    private String uid_username;
    /**
     * 用户id
     */
    private Long uid;
    /**
     * 订单号
     */
    private String sn;
    /**
     * 币类型
     */
    private String token;
    /**
     * 充值金额
     */
    private BigDecimal amount;
    /**
     * 手续费
     */
    private BigDecimal fee;
    /**
     * 实际到账金额
     */
    private BigDecimal real_amount;


    private LocalDateTime create_time;

    private String from_address;

    private String to_address;
    /**
     * 交易哈希
     */
    private String txid;

    private String salesman_username;
    /**
     * 转账网络
     */
    private String currency_type;
}
