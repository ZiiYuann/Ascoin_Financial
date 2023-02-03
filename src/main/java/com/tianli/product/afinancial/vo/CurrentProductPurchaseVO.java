package com.tianli.product.afinancial.vo;

import com.tianli.management.vo.ProductLadderRateVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 产品详情VO
 *
 * @author chenb
 * @apiNote
 * @since 2022-09-06
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CurrentProductPurchaseVO extends FinancialProductVO {

    /**
     * 阶梯化利率
     */
    private List<ProductLadderRateVO> ladderRates;
}
