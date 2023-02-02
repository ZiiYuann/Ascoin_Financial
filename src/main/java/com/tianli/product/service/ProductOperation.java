package com.tianli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.product.dto.PurchaseResultDto;
import com.tianli.product.dto.RedeemResultDto;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.query.PurchaseQuery;
import com.tianli.product.financial.vo.ExpectIncomeVO;

import java.math.BigDecimal;

public interface ProductOperation<T> extends IService<T> {

    /**
     * purchase
     */
    PurchaseResultDto purchase(Long uid, PurchaseQuery purchaseQuery, Order order);

    PurchaseResultDto purchase(Long uid, PurchaseQuery purchaseQuery);

    RedeemResultDto redeem(Long uid, RedeemQuery redeemQuery);

    /**
     * 校验产品是否处于开启状态
     */
    void baseValidProduct(Long uid, FinancialProduct financialProduct, PurchaseQuery purchaseQuery);

    /**
     * 校验余额
     */
    void baseValidRemainAmount(Long uid, String currencyCoin, BigDecimal amount);

    /**
     * 校验申购额度
     */
    void baseValidPurchaseAmount(FinancialProduct product, BigDecimal amount);

    /**
     * 修改推荐状态
     */
    void modifyRecommend(Long id, Boolean recommend, Integer recommendWeight);

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

    /**
     * 校验申购限额
     *
     * @param amount 申购金额
     */
    void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount);

}
