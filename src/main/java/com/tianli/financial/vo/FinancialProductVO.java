package com.tianli.financial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.enums.FinancialProductType;
import com.tianli.financial.enums.PurchaseTerm;
import lombok.Data;

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
     * 图片
     */
    private String logo;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 英文产品名称
     */
    private String nameEn;

    /**
     * 参考年化
     */
    private double rate;

    /**
     * 描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String descriptionEn;

    /**
     * 类型 {@link PurchaseTerm}
     */
    private PurchaseTerm purchaseTerm;

    /**
     * 产品类型 {@link FinancialProductType}
     */
    private FinancialProductType type;
}
