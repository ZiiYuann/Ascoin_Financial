package com.tianli.fund.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RiskType;
import com.tianli.fund.enums.FundRecordStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 基金持有记录
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class FundRecord extends Model<FundRecord> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

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
    private BusinessType businessType;

    /**
     * 年利率
     */
    private BigDecimal rate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态 状态为完成情况 ： holdAmount == 0
     */
    private FundRecordStatus status;

    /**
     * 类型
     */
    private ProductType type;


}
