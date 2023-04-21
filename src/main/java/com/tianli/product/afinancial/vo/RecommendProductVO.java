package com.tianli.product.afinancial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tianli.product.afinancial.enums.ProductType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-12
 **/
@Data
public class RecommendProductVO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 产品名称
     */
    private String name;

    private String coin;

    private String nameEn;

    private String logo;

    /**
     * 产品类型 {@link ProductType}
     */
    private ProductType type;

    /**
     * 参考年化
     */
    private BigDecimal rate;

    private BigDecimal maxRate;

    private BigDecimal minRate;

    private byte rateType;

    private Long recordId;

}
