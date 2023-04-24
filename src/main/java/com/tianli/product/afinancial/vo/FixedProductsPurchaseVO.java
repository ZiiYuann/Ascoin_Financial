package com.tianli.product.afinancial.vo;

import com.tianli.management.vo.ProductLadderRateVO;
import com.tianli.product.afinancial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-07
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixedProductsPurchaseVO {

    private List<FinancialProductVO> products;

    private List<PurchaseTerm> terms;

    /**
     * 阶梯化利率
     */
    private List<ProductLadderRateVO> ladderRates;
}
