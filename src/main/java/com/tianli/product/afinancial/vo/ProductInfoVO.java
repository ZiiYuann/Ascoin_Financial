package com.tianli.product.afinancial.vo;

import com.tianli.product.afinancial.enums.BusinessType;
import com.tianli.product.afinancial.enums.ProductType;
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
