package com.tianli.product.entity;

import com.tianli.product.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductHoldRecord {

    private Long id;

    private Long uid;

    private Long productId;

    private ProductType productType;

    private Long recordId;

}
