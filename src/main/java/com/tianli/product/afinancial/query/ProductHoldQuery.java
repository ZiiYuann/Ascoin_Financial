package com.tianli.product.afinancial.query;

import com.tianli.product.afinancial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    private List<ProductType> productTypes;

    public String getType() {
        return Objects.isNull(productType) ? null : productType.name();
    }
}
