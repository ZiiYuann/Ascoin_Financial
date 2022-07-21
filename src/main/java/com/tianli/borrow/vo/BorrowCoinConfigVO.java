package com.tianli.borrow.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 借币数据配置
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
@Data
public class BorrowCoinConfigVO{

    private static final long serialVersionUID=1L;

    /**
     * 币种
     */
    private String coin;

    /**
     * 最小可借
     */
    private BigDecimal minimumBorrow;

    /**
     * 最大可借
     */
    private BigDecimal maximumBorrow;

    /**
     * 年利率
     */
    private BigDecimal annualInterestRate;

    /**
     * 初始质押率
     */
    private BigDecimal initialPledgeRate;

    /**
     * 可用数额
     */
    private BigDecimal availableAmount;



}
