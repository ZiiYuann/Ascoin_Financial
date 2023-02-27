package com.tianli.account.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.product.afinancial.vo.DollarIncomeVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBalanceMainPageVO extends DollarIncomeVO {

    /**
     * 总资产
     */
    @BigDecimalFormat("0.00")
    private BigDecimal totalAssets;

    @BigDecimalFormat("0.00")
    private BigDecimal totalAccountBalance;

    /**
     * 总可用余额
     */
    @BigDecimalFormat("0.00")
    private BigDecimal totalDollarRemain;

    /**
     * 总理财持有
     */
    @BigDecimalFormat("0.00")
    private BigDecimal totalDollarHold;

    /**
     * 总冻结
     */
    @BigDecimalFormat("0.00")
    private BigDecimal totalDollarFreeze;

    @BigDecimalFormat("0.00")
    private BigDecimal totalDollarPledgeFreeze;

    /**
     * 累计收益
     */
    @BigDecimalFormat("0.00")
    private BigDecimal accrueIncomeFee;

    /**
     * 昨日收益
     */
    @BigDecimalFormat("0.00")
    private BigDecimal yesterdayIncomeFee;


    /**
     * 单个账户余额
     */
    private List<AccountBalanceVO> accountBalances;

}
