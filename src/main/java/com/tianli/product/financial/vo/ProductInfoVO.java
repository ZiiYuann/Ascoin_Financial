package com.tianli.product.financial.vo;

import com.tianli.product.financial.enums.BusinessType;
import com.tianli.product.financial.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-14
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoVO {

    private Long productId;

    private boolean sellOut;

    private boolean newUser;

    private BusinessType businessType;

    private ProductType type;
}
