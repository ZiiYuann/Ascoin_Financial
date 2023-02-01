package com.tianli.product.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.RedisService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.entity.FinancialRecord;
import com.tianli.product.financial.enums.BusinessType;
import com.tianli.product.financial.enums.ProductStatus;
import com.tianli.product.financial.mapper.FinancialProductMapper;
import com.tianli.product.financial.query.PurchaseQuery;
import com.tianli.product.financial.vo.ExpectIncomeVO;
import com.tianli.product.fund.entity.FundRecord;
import com.tianli.product.fund.service.IFundRecordService;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.service.ProductOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-16
 **/
public abstract class AbstractProductOperation<M extends BaseMapper<T>, T> extends ServiceImpl<M, T>
        implements ProductOperation<T> {

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
    @Resource
    private FinancialProductMapper financialProductMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 处理申购结束的hook
     *
     * @param uid           uid
     * @param product       产品
     * @param purchaseQuery 申购参数
     */
    public void finishPurchase(Long uid, FinancialProduct product, PurchaseQuery purchaseQuery) {
    }

    @Override
    public ExpectIncomeVO expectIncome(Long productId, BigDecimal amount) {
        throw new UnsupportedOperationException();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public <R> R purchase(Long uid, PurchaseQuery purchaseQuery, Class<R> rClass, Order order) {
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());

        // pre operation
        this.baseValidProduct(uid, product, purchaseQuery);
        this.baseValidRemainAmount(uid, product.getCoin(), purchaseQuery.getAmount());
        this.baseValidPurchaseAmount(product, purchaseQuery.getAmount());
        this.validPurchaseAmount(uid, product, purchaseQuery.getAmount());

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

    @Override
    public ExpectIncomeVO exceptDailyIncome(BigDecimal holdAmount, BigDecimal rate, int days) {
        return new ExpectIncomeVO(holdAmount.multiply(rate).divide(new BigDecimal(days), 8, RoundingMode.DOWN));
    }

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

    /**
     * 校验账户额度
     *
     * @param uid          用户id
     * @param currencyCoin 币别
     * @param amount       申购金额
     */
    public void baseValidRemainAmount(Long uid, String currencyCoin, BigDecimal amount) {
        AccountBalance accountBalanceBalance = accountBalanceServiceImpl.getAndInit(uid, currencyCoin);
        if (accountBalanceBalance.getRemain().compareTo(amount) < 0) {
            ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
        }
    }

    /**
     * 修改产品推荐状态
     */
    @Transactional
    public void modifyRecommend(Long id, Boolean recommend, Integer recommendWeight) {
        if (Objects.nonNull(recommend)) {
            financialProductMapper.modifyRecommend(id, recommend);
        }
        if (Objects.nonNull(recommendWeight)) {
            financialProductMapper.modifyRecommendWeight(id, recommendWeight);
        }
        stringRedisTemplate.delete(RedisConstants.RECOMMEND_PRODUCT); // 删除缓存
    }

    /**
     * 校验申购金额
     *
     * @param product 产品信息
     * @param amount  金额
     */
    private void baseValidPurchaseAmount(FinancialProduct product, BigDecimal amount) {
        if (product.getTotalQuota() != null && product.getTotalQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(product.getUseQuota()).compareTo(product.getTotalQuota()) > 0) {
            ErrorCodeEnum.PURCHASE_GT_TOTAL_QUOTA.throwException();
        }
    }
}
