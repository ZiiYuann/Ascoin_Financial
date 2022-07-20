package com.tianli.management.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
public class FinancialProductBoardVO {

    // 申购
    private BigDecimal purchaseAmount;
    // 赎回
    private BigDecimal redeemAmount;
    // 结算
    private BigDecimal settleAmount;
    //转存
    private BigDecimal  transferAmount;

    private LocalDate createTime;

    /**
     * 用户累计收益
     */
    private BigDecimal income;

    /**
     * 定期产品持有
     */
    private BigInteger currentProductCount;

    /**
     * 活期产品持有
     */
    private BigInteger fixedProductCount;

    /**
     * 总产品持有
     */
    private BigInteger totalProductCount;

    /**
     * 理财产品持有用户数量
     */
    private BigInteger holdUserCount;

    /**
     * 累计单日收益
     */
    private BigDecimal incomeDaily;
}
