package com.tianli.financial.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.ProductRateDTO;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.mapper.FinancialProductMapper;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.dto.ProductSummaryDataDto;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductEditStatusQuery;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import com.tianli.management.query.FinancialProductsQuery;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.vo.MFinancialProductVO;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianli.common.ConfigConstants.SYSTEM_PURCHASE_MIN_AMOUNT;

@Slf4j
@Service
public class FinancialProductService extends AbstractProductOperation<FinancialProductMapper, FinancialProduct> {

    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private FinancialProductMapper financialProductMapper;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 删除产品
     */
    @Transactional
    public boolean delete(Long productId) {
        FinancialProduct product = financialProductMapper.selectById(productId);
        // 如果产品是基金
        if (ProductType.fund.equals(product.getType())) {
            FundRecordQuery fundRecordQuery = new FundRecordQuery();
            fundRecordQuery.setProductId(productId);
            if (fundRecordService.getHoldUserCount(fundRecordQuery) > 0) {
                ErrorCodeEnum.PRODUCT_USER_HOLD.throwException();
            }
        }

        if (!ProductType.fund.equals(product.getType())) {
            List<FinancialRecord> financialRecords = financialRecordService.selectByProductId(productId);
            Optional<FinancialRecord> match = financialRecords.stream()
                    .filter(financialRecord -> RecordStatus.PROCESS.equals(financialRecord.getStatus())).findAny();
            if (match.isPresent()) {
                log.info("productId ：{} , 用户持有中不允许删除 ", productId);
                ErrorCodeEnum.PRODUCT_USER_HOLD.throwException();
            }
        }

        return financialProductMapper.softDeleteById(productId) > 0;
    }

    /**
     * 保存或者更新产品信息
     */
    @Transactional
    public void saveOrUpdate(FinancialProductEditQuery financialProductQuery) {
        FinancialProduct productDO = financialConverter.toDO(financialProductQuery);
        if (Objects.isNull(financialProductQuery.getLimitPurchaseQuota())) {
            String sysPurchaseMinAmount = configService.get(SYSTEM_PURCHASE_MIN_AMOUNT);
            productDO.setLimitPurchaseQuota(BigDecimal.valueOf(Double.parseDouble(sysPurchaseMinAmount)));
        }

        if (ObjectUtil.isNull(productDO.getId())) {
            productDO.setCreateTime(LocalDateTime.now());
            productDO.setId(CommonFunction.generalId());
            productDO.setUseQuota(BigDecimal.ZERO);
            super.saveOrUpdate(productDO);
        }

        if (Objects.nonNull(productDO.getId())) {
            FinancialProduct product = super.getById(productDO.getId());
            if (ProductStatus.open.equals(product.getStatus())) {
                ErrorCodeEnum.PRODUCT_CAN_NOT_EDIT.throwException();
            }
            // 如果年化利率修改，需要更新持有记录表
            if (!product.getRate().equals(productDO.getRate())) {
                if (ProductType.fund.equals(product.getType())) {
                    fundRecordService.updateRateByProductId(product.getId(), productDO.getRate());
                } else {
                    financialRecordService.updateRateByProductId(product.getId(), productDO.getRate());
                }
            }

            productDO.setUpdateTime(LocalDateTime.now());

            super.updateById(productDO);
        }

        List<FinancialProductLadderRateIoUQuery> ladderRates = financialProductQuery.getLadderRates();
        if (productDO.getRateType() == 1) {
            if (CollectionUtils.isEmpty(ladderRates)) {
                ErrorCodeEnum.throwException("配置为阶段利率模式，阶段利率列表不能为空");
            }
            financialProductLadderRateService.insert(productDO.getId(), ladderRates);
            var maxOptional = ladderRates.stream().map(FinancialProductLadderRateIoUQuery::getRate).max(BigDecimal::compareTo);
            var minOptional = ladderRates.stream().map(FinancialProductLadderRateIoUQuery::getRate).min(BigDecimal::compareTo);
            productDO.setRate(ladderRates.get(0).getRate());
            BigDecimal max = BigDecimal.ZERO;
            BigDecimal min = BigDecimal.ZERO;
            if (maxOptional.isPresent()) {
                max = maxOptional.get();
            }
            if (minOptional.isPresent()) {
                min = minOptional.get();
            }
            productDO.setMinRate(min);
            productDO.setMaxRate(max);
            super.updateById(productDO);


        }

        redisTemplate.delete(RedisConstants.RECOMMEND_PRODUCT);
    }

    /**
     * 修改产品状态
     */
    @Transactional
    public void editProductStatus(FinancialProductEditStatusQuery query) {
        try {

            FinancialProduct product = financialProductMapper.selectById(query.getProductId());
            product = Optional.ofNullable(product).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

            if (ProductStatus.close.equals(query.getStatus())) {
                redisLock.lock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId(), 5L, TimeUnit.SECONDS);
            }

            // 如果是基金产品需要上线，需要查看产品是否与代理人绑定
            if (ProductStatus.open.equals(query.getStatus()) && ProductType.fund.equals(product.getType())) {
                Optional.ofNullable(walletAgentProductService.getByProductId(product.getId()))
                        .orElseThrow(ErrorCodeEnum.FUND_PRODUCT_OPEN_NEED_AGENT::generalException);
            }

            product.setUpdateTime(LocalDateTime.now());
            product.setStatus(query.getStatus());
            if (financialProductMapper.updateById(product) <= 0) {
                ErrorCodeEnum.SYSTEM_ERROR.throwException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId());
        }

    }

    /**
     * 查询产品列表数据
     */
    public IPage<MFinancialProductVO> mSelectListByQuery(IPage<FinancialProduct> page, FinancialProductsQuery query) {
        LambdaQueryWrapper<FinancialProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(FinancialProduct::isDeleted, false);
        if (StringUtils.isNotBlank(query.getName())) {
            queryWrapper = queryWrapper.like(FinancialProduct::getName, query.getName());
        }
        if (Objects.nonNull(query.getType())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getType, query.getType());
        }
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getStatus, query.getStatus());
        }

        if (Objects.nonNull(query.getCoin())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getCoin, query.getCoin());
        }

        queryWrapper = queryWrapper.orderByDesc(FinancialProduct::getCreateTime);

        IPage<FinancialProduct> financialProductIPage = financialProductMapper.selectPage(page, queryWrapper);

        List<Long> productIds = financialProductIPage.getRecords().stream().map(FinancialProduct::getId).collect(Collectors.toList());
        var productSummaryDataDtoMap = financialRecordService.getProductSummaryDataDtoMap(productIds);
        return financialProductIPage.convert(index -> {
            var mFinancialProductVO = managementConverter.toMFinancialProductVO(index);
            var productSummaryDataDto = productSummaryDataDtoMap.getOrDefault(index.getId(), new ProductSummaryDataDto());
            // 设置使用的金额
            mFinancialProductVO.setUseQuota(Optional.ofNullable(index.getUseQuota()).orElse(BigDecimal.ZERO));
            // 设置持有人数

            if (!ProductType.fund.equals(index.getType())) {
                mFinancialProductVO.setHoldUserCount(Optional.ofNullable(productSummaryDataDto.getHoldUserCount()).orElse(BigInteger.ZERO));
            } else {
                // 基金 特殊处理
                Integer holdUserCount = fundRecordService.getHoldUserCount(new FundRecordQuery(index.getId()));
                mFinancialProductVO.setHoldUserCount(BigInteger.valueOf(holdUserCount));
            }

            if (index.getRateType() == 1) {
                mFinancialProductVO.setLadderRates(financialProductLadderRateService.listByProductId(index.getId())
                        .stream().map(financialConverter::toProductLadderRateVO).collect(Collectors.toList()));
            }
            return mFinancialProductVO;
        });
    }

    public IPage<ProductRateDTO> listProductRateDTO(Page<FinancialProduct> page, ProductType productType) {
        return financialProductMapper.listProductRateDTO(page, productType);
    }

    /**
     * 增加额度
     */
    @Transactional
    public void increaseUseQuota(Long productId, BigDecimal increaseAmount, BigDecimal expectAmount) {
        int i = financialProductMapper.increaseUseQuota(productId, increaseAmount, expectAmount);
        if (i <= 0) {
            ErrorCodeEnum.throwException("产品申购额度发生变化，请重新操作");
        }
    }

    /**
     * 减少额度
     */
    @Transactional
    public void reduceUseQuota(Long productId, BigDecimal reduceAmount) {
        int i = financialProductMapper.reduceUseQuota(productId, reduceAmount);
        if (i <= 0) {
            ErrorCodeEnum.throwException("减少产品使用额度失败，请联系管理员");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FinancialPurchaseResultVO purchaseOperation(Long uid, PurchaseQuery purchaseQuery) {
        return purchaseOperation(uid, purchaseQuery, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FinancialPurchaseResultVO purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order) {
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());
        BigDecimal amount = purchaseQuery.getAmount();

//        validRemainAmount(uid, purchaseQuery.getCoin(), amount);
//        validPurchaseAmount(uid, product, amount);

        // 如果是活期，判断是否已经存在申购记录，如果有的话，额外添加待记息金额不生成新的记录
        FinancialRecord financialRecord = FinancialRecord.builder().build();
        Optional<FinancialRecord> recordOptional = Optional.empty();
        if (ProductType.current.equals(product.getType())) {
            recordOptional = financialRecordService.selectByProductId(purchaseQuery.getProductId(), uid)
                    .stream()
                    .sorted(Comparator.comparing(FinancialRecord::getEndTime).reversed())
                    .filter(index -> RecordStatus.PROCESS.equals(index.getStatus())).findFirst();
        }
        // 如果存在申购记录，如果是当天继续申购，则累加金额，否则累加待记利息金额
        if (recordOptional.isPresent()) {
            financialRecord = recordOptional.get();
            financialRecordService.increaseWaitAmount(financialRecord.getId(), amount, financialRecord.getWaitAmount());
        }

        if (recordOptional.isEmpty()) {
            financialRecord = financialRecordService.generateFinancialRecord(uid, product, amount, purchaseQuery.isAutoCurrent());
        }

        if (Objects.isNull(order)) {
            // 生成一笔订单记录
            order = Order.builder()
                    .uid(uid)
                    .coin(product.getCoin())
                    .orderNo(AccountChangeType.purchase.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                    .amount(amount)
                    .type(ChargeType.purchase)
                    .status(ChargeStatus.created)
                    .createTime(LocalDateTime.now())
                    .completeTime(LocalDateTime.now())
                    .status(ChargeStatus.chain_success)
                    .relatedId(financialRecord.getId())
                    .build();
            orderService.save(order);
        }

        // 减少余额
        accountBalanceService.decrease(uid, ChargeType.purchase, product.getCoin(), amount, order.getOrderNo(), CurrencyLogDes.申购.name());

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toFinancialPurchaseResultVO(financialRecord);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setStatusDes(order.getStatus().name());
        financialPurchaseResultVO.setOrderNo(order.getOrderNo());
        return financialPurchaseResultVO;
    }


    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private OrderService orderService;


    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        var productId = product.getId();

        BigDecimal personUse = financialRecordService.getUseQuota(List.of(productId), uid).getOrDefault(productId, BigDecimal.ZERO);

        if (product.getPersonQuota() != null && product.getPersonQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(personUse).compareTo(product.getPersonQuota()) > 0) {
            ErrorCodeEnum.throwException("个人申购额度不足");
        }
    }

    /**
     * 修改产品推荐状态
     */
    @Transactional
    public void modifyRecommend(Long id, Boolean recommend) {
        financialProductMapper.modifyRecommend(id, recommend);
        redisTemplate.delete(RedisConstants.RECOMMEND_PRODUCT);
    }
}
