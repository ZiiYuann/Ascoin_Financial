package com.tianli.management.query;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.*;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Data
public class FinancialProductEditQuery {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 币别
     */
    @NotNull(message = "币别不允许空")
    private CurrencyCoin coin;

    /**
     * 产品名称
     */
    @NotBlank(message = "名称不允许为空")
    private String name;

    /**
     * logo
     */
    @NotBlank(message = "logo不允许为空")
    private String logo;

    /**
     * 产品名称
     */
    @NotBlank(message = "英文名称不允许为空")
    private String nameEn;

    /**
     * 产品类型
     */
    @NotNull(message = "产品类型不允许为null")
    private ProductType type;

    /**
     * 风险类型
     */
    @NotNull(message = "风险类型不允许为null")
    private RiskType riskType;

    /**
     * 类型 {@link PurchaseTerm}
     */
    @NotNull(message = "申购类型不允许为null")
    private PurchaseTerm term;

    @NotNull(message = "运营类型不允许为null")
    private BusinessType businessType;

    /**
     * 参考年化
     */
    @NotNull(message = "日利率不能为空")
    @DecimalMin(value = "0.00000001", message = "日利率填写有误")
    private BigDecimal rate;

    /**
     * 个人额度
     */
    @NotNull(message = "个人额度不能为空")
    @DecimalMin(value = "0.00000001", message = "个人额度填写有误")
    private BigDecimal personQuota;

    /**
     * 总额度
     */
    @NotNull(message = "总额度不能为空")
    @DecimalMin(value = "0.00000001", message = "总额度填写有误")
    private BigDecimal totalQuota;

    /**
     * 产品状态
     */
    private ProductStatus status = ProductStatus.close;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 最小申购限额
     */
    private BigDecimal limitPurchaseQuota;
}
