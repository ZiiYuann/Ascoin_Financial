package com.tianli.product.afinancial.vo;

import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.annotation.BigDecimalFormat;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFinancialVO {

    private Long uid;

    /**
     * 交易类型
     */
    private ChargeType chargeType;

    private String name;

    private String nameEn;

    private String logo;

    private PurchaseTerm term;

    private String coin;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 交易状态
     */
    private ChargeStatus chargeStatus;

    /**
     * 产品类型（冗余）
     */
    private ProductType type;

    /**
     * 金额
     */
    private BigDecimal amount;

    @BigDecimalFormat("0.00")
    private BigDecimal dollarAmount;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 年利率
     */
    private String rate;

    /**
     * 预估收益
     */
    private BigDecimal expectIncome;

    private Long productId;

}
