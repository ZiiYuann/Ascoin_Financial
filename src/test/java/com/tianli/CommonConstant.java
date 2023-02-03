package com.tianli;

import com.tianli.product.afinancial.entity.FinancialProductLadderRate;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.PurchaseTerm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-05
 **/
public class CommonConstant {

    // 活期 普通利率 10% 利率 计息时间：昨天 结束时间：明天 是否阶段利率：否 是否自动续费：否
    public final static String PRODUCT_ONE = "1";
    public final static String PRODUCT_TWO = "2";
    public final static BigDecimal DEFAULT_RATE = BigDecimal.valueOf(0.1f);

    // 活期 普通利率 10% 利率 计息时间：昨天 结束时间：明天 待计息金额：5 记息金额：5
    public final static FinancialRecord record1 = FinancialRecord.builder()
            .id(1L)
            .uid(1L)
            .productId(Long.parseLong(CommonConstant.PRODUCT_ONE))
            .productType(ProductType.current)
            .startIncomeTime(LocalDateTime.now().plusDays(-1))
            .endTime(LocalDateTime.now().plusDays(1))
            .incomeAmount(BigDecimal.valueOf(5L))
            .waitAmount(BigDecimal.valueOf(5L))
            .productTerm(PurchaseTerm.NONE)
            .rate(CommonConstant.DEFAULT_RATE).build();

    // 活期 阶段利率：【0 - 1000】：10% 【1000-3000】： 30%  【3000-5000】： 50%  利率 计息时间：昨天 结束时间：明天 待计息金额：5 记息金额：5
    public final static FinancialRecord record2 = FinancialRecord.builder()
            .id(1L)
            .uid(1L)
            .productId(Long.parseLong(CommonConstant.PRODUCT_TWO))
            .productType(ProductType.current)
            .startIncomeTime(LocalDateTime.now().plusDays(-1))
            .endTime(LocalDateTime.now().plusDays(1))
            .incomeAmount(BigDecimal.valueOf(5000L))
            .waitAmount(BigDecimal.valueOf(5000L))
            .productTerm(PurchaseTerm.NONE)
            .rate(CommonConstant.DEFAULT_RATE).build();

    // 【0 - 1000】：10% 【1000-3000】： 30%  【3000-5000】： 50%
    public final static List<FinancialProductLadderRate> ladderRates = new ArrayList<>();

    static {
        ladderRates.add(FinancialProductLadderRate.builder()
                .startPoint(BigDecimal.ZERO)
                .endPoint(BigDecimal.valueOf(1000L))
                .rate(BigDecimal.valueOf(0.1))
                .build());

        ladderRates.add(FinancialProductLadderRate.builder()
                .startPoint(BigDecimal.valueOf(1000L))
                .endPoint(BigDecimal.valueOf(3000L))
                .rate(BigDecimal.valueOf(0.3))
                .build());

        ladderRates.add(FinancialProductLadderRate.builder()
                .startPoint(BigDecimal.valueOf(3000L))
                .endPoint(null)
                .rate(BigDecimal.valueOf(0.5))
                .build());
    }

}
