package com.tianli.fund.vo;

import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class FundRecordVO implements Serializable {
    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

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
    private String riskType;

    /**
     * 运营类型
     */
    private String businessType;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 交易类型
     */
    private String type;

    /**
     * 交易状态
     */
    private Integer status;

    /**
     * 累计收益
     */
    private BigDecimal cumulativeIncomeAmount;

    /**
     * 昨日收益
     */
    private BigDecimal lastIncome;


}
