package com.tianli.dividends.settlement.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 低级代理商保证金
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class LowSettlement {

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
    private LowSettlementChargeType charge_type;

    /**
     * 备注
     */
    private String note;

}
