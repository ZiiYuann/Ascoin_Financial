package com.tianli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.charge.entity.Order;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.query.PurchaseQuery;
import com.tianli.product.financial.vo.ExpectIncomeVO;

import java.math.BigDecimal;

public interface ProductOperation<T> extends IService<T> {

    /**
     * 校验申购限额
     *
     * @param amount 申购金额
     */
    void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount);

    /**
     * @param uid           uid
     * @param purchaseQuery 申购参数
     * @param order         外部order
     * @param <R>           r
     */
    <R> R purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order);

    /**
     * 预计收益
     *
     * @param productId 产品id
     * @param amount    金额
     * @return 收益VO
     */
    ExpectIncomeVO expectIncome(Long productId, BigDecimal amount);

    /**
     * 预计每日收益
     *
     * @param recordId 持有记录id
     * @return 每日收益VO
     */
    ExpectIncomeVO exceptDailyIncome(Long uid, Long productId, Long recordId);

    ExpectIncomeVO exceptDailyIncome(BigDecimal holdAmount, BigDecimal rate, int days);

    /**
     * 预计收益利率
     *
     * @param uid       用户id
     * @param productId 产品id
     * @param recordId  持有记录id
     * @return 收益率
     */
    BigDecimal incomeRate(Long uid, Long productId, Long recordId);
}
