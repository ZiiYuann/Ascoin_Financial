package com.tianli.product.financial.vo;

import com.tianli.product.financial.enums.PurchaseTerm;
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
}
