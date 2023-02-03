package com.tianli.product.financial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.RedisService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.dto.PurchaseResultDto;
import com.tianli.product.dto.RedeemResultDto;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.financial.dto.IncomeDto;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.entity.FinancialRecord;
import com.tianli.product.financial.enums.BusinessType;
import com.tianli.product.financial.enums.ProductStatus;
import com.tianli.product.financial.enums.ProductType;
import com.tianli.product.financial.mapper.FinancialProductMapper;
import com.tianli.product.financial.query.PurchaseQuery;
import com.tianli.product.financial.vo.ExpectIncomeVO;
import com.tianli.product.fund.entity.FundRecord;
import com.tianli.product.fund.service.IFundRecordService;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.service.FundProductService;
import com.tianli.product.service.ProductHoldRecordService;
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
    @Resource
    private ProductHoldRecordService productHoldRecordService;
    @Resource
    private FundProductService fundProductService;


    /**
     * purchase Operation
     */
    public abstract PurchaseResultDto purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order);

    /**
     * redeem Operation
     */
    public abstract RedeemResultDto redeemOperation(Long uid, RedeemQuery redeemQuery);

    /**
     * 统计利息
     */
    public abstract IncomeDto incomeOperation(Long uid, Long productId, Long record);

    /**
     * 处理申购结束的hook
     *
     * @param uid           uid
     * @param product       产品
     * @param purchaseQuery 申购参数
     */
    public void finishPurchase(Long uid, FinancialProduct product, PurchaseQuery purchaseQuery) {
    }

    /**
     * 各自产品类型校验
     *
     * @param financialProduct 产品信息
     * @param purchaseQuery    申购请求
     */
    public void validProduct(FinancialProduct financialProduct, PurchaseQuery purchaseQuery) {
    }


    @Override
    public ExpectIncomeVO expectIncome(Long productId, BigDecimal amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IncomeDto income(ProductType type, Long uid, Long productId, Long recordId) {
        if (ProductType.fund.equals(type)) {
            return fundProductService.incomeOperation(uid, productId, recordId);
        }
        return financialProductService.incomeOperation(uid, productId, recordId);
    }

    @Transactional
    public PurchaseResultDto purchase(Long uid, PurchaseQuery purchaseQuery, Order order) {
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());

        // pre operation
        this.baseValidProduct(uid, product, purchaseQuery);
        this.baseValidRemainAmount(uid, product.getCoin(), purchaseQuery.getAmount());
        this.baseValidPurchaseAmount(product, purchaseQuery.getAmount());
        this.validPurchaseAmount(uid, product, purchaseQuery.getAmount());

        PurchaseResultDto resultDto = this.purchaseOperation(uid, purchaseQuery, order);

        // finish operation
        productHoldRecordService.saveDo(ProductHoldRecord.builder().uid(uid)
                .productType(product.getType())
                .productId(purchaseQuery.getProductId())
                .recordId(resultDto.getRecordId())
                .build());
        financialProductService.increaseUseQuota(product.getId(), purchaseQuery.getAmount(), product.getUseQuota());
        this.finishPurchase(uid, product, purchaseQuery);

        return resultDto;
    }

    @Transactional
    public PurchaseResultDto purchase(Long uid, PurchaseQuery purchaseQuery) {
        return purchase(uid, purchaseQuery, null);
    }

    /**
     * redeem product
     *
     * @param uid         uid
     * @param redeemQuery query
     * @return result dto
     */
    @Transactional
    public RedeemResultDto redeem(Long uid, RedeemQuery redeemQuery) {

        // pre redeem

        RedeemResultDto resultDto = this.redeemOperation(uid, redeemQuery);

        // after redeem
        return resultDto;
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
     * 校验申购金额
     *
     * @param product 产品信息
     * @param amount  金额
     */
    public void baseValidPurchaseAmount(FinancialProduct product, BigDecimal amount) {
        if (product.getTotalQuota() != null && product.getTotalQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(product.getUseQuota()).compareTo(product.getTotalQuota()) > 0) {
            ErrorCodeEnum.PURCHASE_GT_TOTAL_QUOTA.throwException();
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

}
