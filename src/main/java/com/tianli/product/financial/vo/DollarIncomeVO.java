package com.tianli.product.financial.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.product.financial.enums.ProductType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-11
 **/
@Data
public class DollarIncomeVO {

    /**
     * 持有币
     */
    @BigDecimalFormat("0.00")
    private BigDecimal holdFee;

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
     * 不同类型产品收益
     */
    private Map<ProductType,DollarIncomeVO> incomeMap;

}
