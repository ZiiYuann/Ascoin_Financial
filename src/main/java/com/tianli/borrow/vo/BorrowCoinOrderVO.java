package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 借币订单
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
public class BorrowCoinOrderVO {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 币别图标
     */
    private String logo;

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
    @BigDecimalFormat("#.##%")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime borrowTime;

    /**
     * 借款时长
     */
    @BigDecimalFormat
    private BigDecimal borrowDuration;

    /**
     * 年利率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal annualInterestRate;

    /**
     * 结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime settlementTime;

}
