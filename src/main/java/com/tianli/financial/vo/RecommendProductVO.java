package com.tianli.financial.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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

}
