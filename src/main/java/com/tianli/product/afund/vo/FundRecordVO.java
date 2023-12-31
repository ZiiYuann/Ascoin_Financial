package com.tianli.product.afund.vo;

import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RiskType;
import com.tianli.product.afund.enums.FundRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundRecordVO implements Serializable {
    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 英文名称
     */
    private String productNameEn;

    /**
     * 币种
     */
    private String coin;

    /**
     * logo
     */
    private String logo;

    /**
     * 持有金额
     */
    private BigDecimal holdAmount;

    /**
     * 累计收益
     */
    private BigDecimal cumulativeIncomeAmount;

    /**
     * 已发放收益
     */
    private BigDecimal incomeAmount;

    /**
     * 待发放收益
     */
    private BigDecimal waitIncomeAmount;

    /**
     * 风险类型
     */
    private RiskType riskType;

    /**
     * 运营类型
     */
    private String businessType;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 产品类型
     */
    private ProductType type;

    /**
     * 交易状态
     */
    private FundRecordStatus status;

    /**
     * 昨日收益
     */
    private BigDecimal yesterdayIncomeAmount;

    /**
     * 是否允许赎回
     */
    private Boolean isAllowRedemption;

    private boolean sellOut;

    private BigDecimal earningRate;

}
