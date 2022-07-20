package com.tianli.borrow.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 借币还款记录
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Data
public class BorrowRepayRecordVO{

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
     * 币别
     */
    private String coin;

    /**
     * 还款金额
     */
    private BigDecimal repayAmount;

    /**
     * 本金还款
     */
    private BigDecimal repayCapital;

    /**
     * 利息还款
     */
    private BigDecimal repayInterest;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 还款时间
     */
    private Date repayTime;

    /**
     * 创建时间
     */
    private Date createTime;

}
