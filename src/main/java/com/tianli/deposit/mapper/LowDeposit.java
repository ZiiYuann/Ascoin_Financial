package com.tianli.deposit.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 低级代理商保证金
 * </p>
 *
 * @author hd
 * @since 2020-12-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LowDeposit {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 高级代理商id
     */
    private Long senior_uid;

    /**
     * 低级代理商id
     */
    private Long low_uid;

    /**
     * 金额
     */
    private BigInteger amount;

    /**
     * 交易类型
     */
    private LowDepositChargeType charge_type;

    /**
     * 备注
     */
    private String note;
}
