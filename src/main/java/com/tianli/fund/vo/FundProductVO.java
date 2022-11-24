package com.tianli.fund.vo;

import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RiskType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundProductVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 币别
     */
    private String coin;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * logo
     */
    private String logo;

    /**
     * 产品类型
     */
    private ProductType type;

    /**
     * 参考年化
     */
    private BigDecimal rate;

    /**
     * 风险等级
     */
    private RiskType riskType;

    /**
     * 业务类型
     */
    private BusinessType businessType;

    /**
     * 总使用额度
     */
    private BigDecimal useQuota;

    private BigDecimal totalQuota;
}
