package com.tianli.fund.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RiskType;
import com.tianli.fund.enums.FundRecordStatus;
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
    private CurrencyCoin coin;

    /**
     * logo
     */
    private String logo;

    /**
     * 持有金额
     */
    private BigDecimal holdAmount;

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
     * 昨日收益
     */
    private BigDecimal lastIncome;

    /**
     * 是否允许赎回
     */
    private Boolean isAllowRedemption;

}
