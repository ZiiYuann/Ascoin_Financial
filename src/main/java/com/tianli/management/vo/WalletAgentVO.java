package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletAgentVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 代理人ID
     */
    private Long uid;

    /**
     * 代理人名称
     */
    private String agentName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 云钱余额
     */
    private BigDecimal walletAmount;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 提现金额
     */
    private BigDecimal withdrawAmount;

    /**
     * 持仓金额
     */
    private BigDecimal HoldAmount;

    /**
     * 待赎回金额
     */
    private BigDecimal redeemAmount;

    /**
     * 已发利息
     */
    private BigDecimal payInterestAmount;

    /**
     * 待发利息
     */
    private BigDecimal waitPayInterestAmount;


    /**
     * 子产品列表
     */
    private List<Product> products;

    @Data
    @Builder
    public static class Product{

        //产品ID
        private Long productId;

        //产品名称
        private String productName;

        //推荐码
        private String referralCode;

    }
}
