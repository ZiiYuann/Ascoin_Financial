package com.tianli.management.query;

import com.tianli.product.afinancial.enums.ProductStatus;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Data
public class FinancialProductEditStatusQuery {

    private Long productId;

    private ProductStatus status;
}
