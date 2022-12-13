package com.tianli.financial.vo;

import com.tianli.financial.enums.ProductType;
import lombok.Data;

import java.math.BigDecimal;


/**
 * 持有产品信息
 *
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Data
public class HoldProductVo {

    /**
     * 记录id
     */
    private Long productId;

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 活期/定期产品
     */
    private ProductType productType;

    /**
     * 产品名称
     */
    private String name;

    /**
     * nameEn
     */
    private String nameEn;

    private BigDecimal holdAmount;

    /**
     * 收益信息
     */
    private IncomeVO incomeVO;

    private String logo;

    private String coin;

    private BigDecimal accrueIncomeAmount;

}
