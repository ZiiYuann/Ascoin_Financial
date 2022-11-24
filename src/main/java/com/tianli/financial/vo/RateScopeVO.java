package com.tianli.financial.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-29
 **/
@Data
public class RateScopeVO {

    /**
     * 币别
     */
    private String coin;

    /**
     * logo 地址
     */
    private String logo;

    /**
     * 最大利率
     * @since 1.1
     */
    private BigDecimal maxRate;

    /**
     * 最小利率
     */
    private BigDecimal minRate;

    /**
     * 产品列表
     */
    private List<FinancialProductVO> products;
}
