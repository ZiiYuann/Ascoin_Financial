package com.tianli.product.financial.dto;

import com.tianli.product.financial.entity.FinancialIncomeAccrue;
import com.tianli.product.financial.enums.ProductStatus;
import com.tianli.product.financial.enums.ProductType;
import com.tianli.product.financial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinancialIncomeAccrueDTO extends FinancialIncomeAccrue {

    private String coin;

    private String productName;

    private ProductStatus productStatus;

    private PurchaseTerm productTerm;

    /**
     * 活期/定期产品
     */
    private ProductType productType;

    private BigDecimal rate;

    private BigDecimal holdAmount;

    private String logo;

    private String productNameEn;
}
