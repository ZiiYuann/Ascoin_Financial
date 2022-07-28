package com.tianli.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 借币利息记录
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
public class BorrowInterestRecordVO{

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 币种
     */
    private String coin;

    /**
     * 待付本金
     */
    private BigDecimal waitRepayCapital;

    /**
     * 待付利息
     */
    private BigDecimal waitRepayInterest;

    /**
     * 本次计息
     */
    private BigDecimal interestAccrual;

    /**
     * 计息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime interestAccrualTime;


}
