package com.tianli.management.spot.vo;

import com.tianli.charge.mapper.ChargeStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/15 3:41 下午
 */
@Data
public class SGWithdrawListVo {
    /**
     * id
     */
    private Long id;
    /**
     * 用户邮箱
     */
    private String uid_username;
    /**
     * 订单状态
     */
    private ChargeStatus status;
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
     * 提现金额
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

    private String reviewer;

    private Long reviewer_id;

    private LocalDateTime reviewer_time;

    private String reason;

    private String reason_en;

    private String from_address;

    private String to_address;

    private String note;

    private String review_note;

    private Integer user_type;
    /**
     * 转账网络
     */
    private String currency_type;

}


