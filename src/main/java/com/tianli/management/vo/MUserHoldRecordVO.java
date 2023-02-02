package com.tianli.management.vo;

import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.product.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MUserHoldRecordVO {

    private Long uid;

    @BigDecimalFormat("0.00")
    private BigDecimal holdFee;

    private Map<ProductType,String> holdFeeMap;

    @BigDecimalFormat("0.00")
    private BigDecimal calIncomeFee;

    @BigDecimalFormat("0.00")
    private BigDecimal waitIncomeFee;

    @BigDecimalFormat("0.00")
    private BigDecimal accrueIncomeAmount;


}
