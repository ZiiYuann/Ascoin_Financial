package com.tianli.product.financial.query;

import com.tianli.product.financial.enums.PurchaseTerm;
import lombok.*;

import java.math.BigDecimal;

/**
 * @apiNote  申购请求
 * @author chenb
 * @since 2022-07-06 11:22
 **/
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuery {

   /**
    * 产品id
    */
    private Long productId;

    /**
     * 申购数量
     */
    private BigDecimal amount;

    /**
     * 申购币别
     */
    private String coin;

    /**
     * 申购期限
     */
    private PurchaseTerm term;

    /**
     * 是否自动活期
     */
    private boolean autoCurrent;
}
