package com.tianli.borrow.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;

<<<<<<< HEAD
import lombok.Builder;
=======
>>>>>>> origin/dev_aws
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
<<<<<<< HEAD
@Builder
=======
>>>>>>> origin/dev_aws
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
    private BigDecimal currentPledgeRate;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 借出时间
     */
    private Date borrowTime;

    /**
     * 借款时长
     */
    private BigDecimal borrowDuration;

    /**
     * 结算时间
     */
    private Date settlementTime;

    /**
     * 创建时间
     */
    private Date createTime;

}
