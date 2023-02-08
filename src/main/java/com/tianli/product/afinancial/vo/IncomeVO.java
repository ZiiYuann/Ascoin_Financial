package com.tianli.product.afinancial.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.product.afinancial.enums.ProductType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author chenb
 * @apiNote 收益汇总VO
 * @since 2022-07-13
 **/
@Data
public class IncomeVO {

    /**
     * 持有币
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal holdFee;

    /**
     * 累计收益u
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal accrueIncomeFee;

    /**
     * 昨日收益u
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal yesterdayIncomeFee;

    /**
     * 昨日收益u
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal waitAuditIncomeFee;

    /**
     * 每日收益
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal dailyIncomeFee;

    /**
     * 累计收益当前币
     */
    private BigDecimal accrueIncomeAmount;

    /**
     * 昨日收益
     */
    private BigDecimal yesterdayIncomeAmount;

    /**
     * 待审核收益
     */
    private BigDecimal waitAuditIncomeAmount;

    /**
     * 不同类型产品收益
     */
    private Map<ProductType, IncomeVO> incomeMap;

}
