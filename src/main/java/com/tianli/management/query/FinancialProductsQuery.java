package com.tianli.management.query;

import com.tianli.product.afinancial.enums.ProductStatus;
import com.tianli.product.afinancial.enums.ProductType;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Data
public class FinancialProductsQuery {

    private String name;

    private ProductType type;

    private ProductStatus status;

    private String coin;

    private Boolean recommend;
}
