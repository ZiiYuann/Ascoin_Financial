package com.tianli.borrow.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 借币数据配置
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
@Data
@Builder
public class BorrowApplePageVO {

    private static final long serialVersionUID=1L;

    /**
     * 币种
     */
    private String borrowCoin;

    /**
     * logo
     */
    private String logo;

    /**
     * 最小可借
     */
    @BigDecimalFormat
    private BigDecimal minimumBorrow;

    /**
     * 最大可借
     */
    @BigDecimalFormat
    private BigDecimal maximumBorrow;

    /**
     * 年利率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal annualInterestRate;

    /**
     * 初始质押率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal initialPledgeRate;

    /**
     * 强制平仓质押率
     */
    @BigDecimalFormat("#.##%")
    private BigDecimal liquidationPledgeRate;

    /**
     * 可用数额
     */
    @BigDecimalFormat
    private BigDecimal availableAmount;



}
