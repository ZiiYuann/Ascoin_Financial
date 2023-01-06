package com.tianli.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.RedisService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.service.IFundRecordService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-16
 **/
public abstract class AbstractProductOperation<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    @Resource
    private RedisService redisService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private FinancialRecordService financialRecordService;


    @Transactional
    @SuppressWarnings("unchecked")
    public <R> R purchase(Long uid, PurchaseQuery purchaseQuery, Class<R> rClass, Order order) {
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());

        // pre operation
        baseValidProduct(uid, product, purchaseQuery);
        validRemainAmount(uid, product.getCoin(), purchaseQuery.getAmount());
        baseValidPurchaseAmount(uid, product, purchaseQuery.getAmount());


        Object o = this.purchaseOperation(uid, purchaseQuery, order);


        // finish operation
        financialProductService.increaseUseQuota(product.getId(), purchaseQuery.getAmount(), product.getUseQuota());

        finishPurchase(uid, product, purchaseQuery);

        if (!o.getClass().equals(rClass)) {
            throw new UnsupportedOperationException();
        }
        return (R) o;
    }

    @Transactional
    public <R> R purchase(Long uid, PurchaseQuery purchaseQuery, Class<R> rClass) {
        return purchase(uid, purchaseQuery, rClass, null);
    }

    public abstract <R> R purchaseOperation(Long uid, PurchaseQuery purchaseQuery);

    /**
     * @param uid           uid
     * @param purchaseQuery 申购参数
     * @param order         外部order
     * @param <R>           r
     * @return
     */
    public abstract <R> R purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order);

    /**
     * 校验产品是否处于开启状态
     *
     * @param financialProduct 产品
     */
    public void baseValidProduct(Long uid, FinancialProduct financialProduct, PurchaseQuery purchaseQuery) {
        BigDecimal purchaseAmount = purchaseQuery.getAmount();
        if (Objects.isNull(financialProduct)) {
            throw ErrorCodeEnum.PRODUCT_CAN_NOT_BUY.generalException();
        }

        Long productId = financialProduct.getId();
        boolean exists = redisService.exists(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + productId);
        if (exists) {
            ErrorCodeEnum.PRODUCT_CAN_NOT_BUY.throwException();
        }

        if (ProductStatus.open != financialProduct.getStatus()) {
            ErrorCodeEnum.NOT_OPEN.throwException();
        }

        if (purchaseAmount.compareTo(financialProduct.getLimitPurchaseQuota()) < 0) {
            throw ErrorCodeEnum.PURCHASE_AMOUNT_TO_SMALL.generalException("低于最小申购数额:" + financialProduct.getLimitPurchaseQuota());
        }

        if (BusinessType.benefits.equals(financialProduct.getBusinessType())) {
            int fundHoldAmount = fundRecordService.count(new LambdaQueryWrapper<FundRecord>().eq(FundRecord::getUid, uid));
            int financialHoldAmount = financialRecordService.count(new LambdaQueryWrapper<FinancialRecord>().eq(FinancialRecord::getUid, uid));
            if (fundHoldAmount > 0 || financialHoldAmount > 0) {
                ErrorCodeEnum.BENEFITS_NOT_BUY.throwException();
            }
        }

        this.validProduct(financialProduct, purchaseQuery);

    }

    public void validProduct(FinancialProduct financialProduct, PurchaseQuery purchaseQuery) {

    }

    public void baseValidPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {

        if (product.getTotalQuota() != null && product.getTotalQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(product.getUseQuota()).compareTo(product.getTotalQuota()) > 0) {
            ErrorCodeEnum.PURCHASE_GT_TOTAL_QUOTA.throwException();
        }

        this.validPurchaseAmount(uid, product, amount);

    }

    /**
     * 校验账户额度
     *
     * @param amount 申购金额
     */
    public void validRemainAmount(Long uid, String currencyCoin, BigDecimal amount) {
        AccountBalance accountBalanceBalance = accountBalanceServiceImpl.getAndInit(uid, currencyCoin);
        if (accountBalanceBalance.getRemain().compareTo(amount) < 0) {
            ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
        }
    }

    /**
     * 校验申购限额
     *
     * @param amount 申购金额
     */
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        throw new UnsupportedOperationException();
    }

    public void finishPurchase(Long uid, FinancialProduct product, PurchaseQuery purchaseQuery) {

    }
}
