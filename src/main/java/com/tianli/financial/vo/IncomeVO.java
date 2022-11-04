package com.tianli.financial.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.financial.enums.ProductType;
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
     * 累计收益
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal accrueIncomeFee;

    /**
     * 昨日收益
     */
    @BigDecimalFormat("0.00######")
    private BigDecimal yesterdayIncomeFee;

    /**
     * 昨日收益
     */
    private BigDecimal yesterdayIncomeAmount;

    /**
     * 不同类型产品收益
     */
    private Map<ProductType,IncomeVO> incomeMap;

}
