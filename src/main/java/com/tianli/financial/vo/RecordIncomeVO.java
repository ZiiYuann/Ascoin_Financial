package com.tianli.financial.vo;

import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.enums.RiskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordIncomeVO {

    private String productName;

    private String productNameEn;

    private ProductType productType;

    private PurchaseTerm productTerm;

    private BigDecimal rate;

    private String coin;

    private RiskType riskType;

    private String logo;

    /**
     * 持有币
     */
    private BigDecimal holdAmount;

    /**
     * 待计息金额
     */
    private BigDecimal waitAmount;

    /**
     * 计息金额
     */
    private BigDecimal incomeAmount;

    /**
     * 累计收益
     */
    private BigDecimal accrueIncomeFee;

    /**
     * 昨日收益
     */
    private BigDecimal yesterdayIncomeFee;

    private RecordStatus recordStatus;

    /**
     * 自动续费
     */
    private boolean autoRenewal;

    private Long productId;

    private byte rateType;

    private BigDecimal maxRate;

    private BigDecimal minRate;

    private boolean sellOut;

}
