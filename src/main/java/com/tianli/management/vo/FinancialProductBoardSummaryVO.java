package com.tianli.management.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-16
 **/
@Data
@Builder
public class FinancialProductBoardSummaryVO {

    // 申购
    private BigDecimal purchaseAmount;
    // 赎回
    private BigDecimal redeemAmount;
    // 结算
    private BigDecimal settleAmount;
    //转存
    private BigDecimal  transferAmount;

    /**
     * 用户累计收益
     */
    private BigDecimal income;

    /**
     * 定期产品持有U
     */
    private BigDecimal currentProductCount;

    /**
     * 活期产品持有U
     */
    private BigDecimal fixedProductCount;

    /**
     * 理财产品持有用户数量
     */
    private BigInteger holdUserCount;

    // 详情
    private List<FinancialProductBoardVO> data;

}
