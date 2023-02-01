package com.tianli.product.financial.query;

import com.tianli.product.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-31
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductHoldQuery {

    private Long uid;

    private String type;

    private Long productId;

    private ProductType productType;

    public String getType() {
        return Objects.isNull(productType) ? null : productType.name();
    }
}
