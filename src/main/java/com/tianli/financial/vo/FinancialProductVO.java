package com.tianli.financial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-12
 **/
@Data
public class FinancialProductVO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品类型 {@link ProductType}
     */
    private ProductType type;

    /**
     * 风险类型
     */
    private RiskType riskType;

    /**
     * 类型 {@link PurchaseTerm}
     */
    private PurchaseTerm term;


    /**
     * 产品状态 {@link ProductStatus}
     */
    private ProductStatus status;

    private BusinessType businessType;

    /**
     * 参考年化
     */
    private double rate;

    /**
     * 已经使用
     */
    private BigDecimal useQuota;

    private BigDecimal personQuota;

    /**
     * 总额度
     */
    private BigDecimal totalQuota;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
