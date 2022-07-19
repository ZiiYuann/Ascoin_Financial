package com.tianli.financial.vo;

import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.PurchaseTerm;
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

    /**
     * 交易类型
     */
    private ChargeType chargeType;

    private String name;
    private String nameEn;

    private PurchaseTerm term;

    private CurrencyCoin coin;

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



}
