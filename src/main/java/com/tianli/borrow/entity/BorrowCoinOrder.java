package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 借币订单
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class BorrowCoinOrder extends Model<BorrowCoinOrder> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 借出币种
     */
    private String borrowCoin;

    /**
     * 借出本金
     */
    private BigDecimal borrowCapital;

    /**
     * 累计利息
     */
    private BigDecimal cumulativeInterest;

    /**
     * 已还金额
     */
    private BigDecimal repayAmount;

    /**
     * 待还本金
     */
    private BigDecimal waitRepayCapital;

    /**
     * 待还利息
     */
    private BigDecimal waitRepayInterest;

    /**
     * 质押币种
     */
    private String pledgeCoin;

    /**
     * 质押金额
     */
    private BigDecimal pledgeAmount;

    /**
     * 当前质押率
     */
    private BigDecimal pledgeRate;

    /**
     * 质押状态
     */
    private Integer pledgeStatus;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 借出时间
     */
    private LocalDateTime borrowTime;

    /**
     * 借款时长
     */
    private Long borrowDuration;

    /**
     * 结算时间
     */
    private LocalDateTime settlementTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
