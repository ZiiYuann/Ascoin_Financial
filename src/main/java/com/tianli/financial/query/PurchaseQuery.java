package com.tianli.financial.query;

import com.tianli.financial.enums.PurchaseTerm;
import lombok.Getter;

import javax.validation.constraints.DecimalMin;

/**
 * @apiNote  申购请求
 * @author chenb
 * @since 2022-07-06 11:22
 **/
@Getter
public class PurchaseQuery {

   /**
    * 产品id
    */
    private Long productId;

    /**
     * 申购数量
     */
    @DecimalMin(value = "10", message = "最低申购数量为10")
    private double amount;

    /**
     * 申购期限
     */
    private PurchaseTerm term;

    /**
     * 是否自动活期
     */
    private boolean autoCurrent;
}
