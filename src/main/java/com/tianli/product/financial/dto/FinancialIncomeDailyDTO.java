package com.tianli.product.financial.dto;

import com.tianli.product.financial.entity.FinancialIncomeDaily;
import com.tianli.product.financial.enums.ProductStatus;
import com.tianli.product.financial.enums.ProductType;
import com.tianli.product.financial.enums.PurchaseTerm;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
public class FinancialIncomeDailyDTO extends FinancialIncomeDaily {

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
}
