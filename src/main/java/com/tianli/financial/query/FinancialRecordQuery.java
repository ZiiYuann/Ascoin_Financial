package com.tianli.financial.query;

import com.tianli.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-31
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialRecordQuery {

    private ProductType productType;

    private Long uid;

    private String coin;

    public FinancialRecordQuery(ProductType productType) {
        this.productType = productType;
    }
}
