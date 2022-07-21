package com.tianli.charge.vo;

import com.tianli.charge.enums.ChargeStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.PurchaseTerm;
import com.tianli.financial.enums.RiskType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
public class OrderBaseVO {

    /**
     * 产品名称
     */
    private String productName;

    private String productNameEn;

    private String logo;

    private ProductType productType;

    private PurchaseTerm productTerm;

    private RiskType riskType;

    private CurrencyCoin coin;

    private String orderNo;

    /**
     * 参考年化
     */
    private double rate;

    /**
     * 申购数额、赎回数额、转存数额、结算数额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     */
    private ChargeStatus orderStatus;

    /**
     * 开始计息时间
     */
    private LocalDateTime startIncomeTime;

    /**
     * 结算时间
     */
    private LocalDateTime endTime;

    /**
     * 赎回时间
     */
    private LocalDateTime redeemTime;

    /**
     * 自动续费
     */
    private boolean autoRenewal;
}
