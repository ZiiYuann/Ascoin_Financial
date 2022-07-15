package com.tianli.financial.vo;

import com.tianli.financial.enums.ProductType;
import lombok.Data;


/**
 * 持有产品信息
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Data
public class HoldProductVo {

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 活期/定期产品
     */
    private ProductType financialProductType;

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
     * 收益信息
     */
    private IncomeVO incomeVO;
}
