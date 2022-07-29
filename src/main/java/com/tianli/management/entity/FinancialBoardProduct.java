package com.tianli.management.entity;

import com.tianli.common.CommonFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialBoardProduct {

    @Id
    private Long id;

    /**
     * 申购
     */
    private BigDecimal purchaseAmount;

    /**
     * 赎回
     */
    private BigDecimal redeemAmount;

    /**
     * 结算
     */
    private BigDecimal settleAmount;

    /**
     * 转存
     */
    private BigDecimal transferAmount;

    /**
     * 用户累计收益
     */
    private BigDecimal income;

    /**
     * 活期产品持有
     */
    private BigInteger currentProductCount;

    /**
     * 定期产品持有
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

    private LocalDate createTime;

    public static FinancialBoardProduct getDefault(){
        return FinancialBoardProduct.builder()
                .id(CommonFunction.generalId())
                .income(BigDecimal.ZERO)
                .purchaseAmount(BigDecimal.ZERO)
                .redeemAmount(BigDecimal.ZERO)
                .settleAmount(BigDecimal.ZERO)
                .transferAmount(BigDecimal.ZERO)
                .income(BigDecimal.ZERO)
                .currentProductCount(BigInteger.ZERO)
                .fixedProductCount(BigInteger.ZERO)
                .totalProductCount(BigInteger.ZERO)
                .holdUserCount(BigInteger.ZERO).build();
    }
}
