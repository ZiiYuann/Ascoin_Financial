package com.tianli.product.fund.bo;

import com.tianli.product.financial.query.PurchaseQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class FundPurchaseBO extends PurchaseQuery {

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @DecimalMin(value = "0.00000001", message = "调整金额必须大于0")
    private BigDecimal purchaseAmount;

    @NotBlank(message = "推荐码不能为空")
    private String referralCode;

    @Override
    public BigDecimal getAmount() {
        return purchaseAmount;
    }
}
