package com.tianli.product.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.dto.ProductSummaryDataDto;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductEditStatusQuery;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import com.tianli.management.query.FinancialProductsQuery;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.vo.MFinancialProductVO;
import com.tianli.mconfig.ConfigService;
import com.tianli.product.dto.PurchaseResultDto;
import com.tianli.product.dto.RedeemResultDto;
import com.tianli.product.afinancial.convert.FinancialConverter;
import com.tianli.product.afinancial.dto.IncomeDto;
import com.tianli.product.afinancial.dto.ProductRateDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeAccrue;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductStatus;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RecordStatus;
import com.tianli.product.afinancial.mapper.FinancialProductMapper;
import com.tianli.product.afinancial.query.PurchaseQuery;
import com.tianli.product.afinancial.service.AbstractProductOperation;
import com.tianli.product.afinancial.service.FinancialIncomeAccrueService;
import com.tianli.product.afinancial.service.FinancialProductLadderRateService;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afinancial.vo.ExpectIncomeVO;
import com.tianli.product.afinancial.vo.FinancialPurchaseResultVO;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.service.IFundRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
    private FinancialIncomeAccrueService financialIncomeAccrueService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private OrderService orderService;
    @Resource
    private ProductHoldRecordService productHoldRecordService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
            boolean rateChange = !product.getRate().equals(productDO.getRate());
            if (rateChange && ProductType.fund.equals(product.getType())) {
                fundRecordService.updateRateByProductId(product.getId(), productDO.getRate());
            }
            if (rateChange && !ProductType.fund.equals(product.getType())) {
                financialRecordService.updateRateByProductId(product.getId(), productDO.getRate());
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
            var max = ladderRates.stream().map(FinancialProductLadderRateIoUQuery::getRate)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            var min = ladderRates.stream().map(FinancialProductLadderRateIoUQuery::getRate)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            productDO.setRate(ladderRates.get(0).getRate());

            productDO.setMinRate(min);
            productDO.setMaxRate(max);
            super.updateById(productDO);
        }

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
                product.setRecommend(false);
                redisLock.lock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId(), 5L, TimeUnit.SECONDS);
            }

            // 如果是基金产品需要上线，需要查看产品是否与代理人绑定
            if (ProductStatus.open.equals(query.getStatus())
                    && ProductType.fund.equals(product.getType())
                    && Objects.isNull(walletAgentProductService.getByProductId(product.getId()))) {
                throw ErrorCodeEnum.FUND_PRODUCT_OPEN_NEED_AGENT.generalException();
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
            stringRedisTemplate.delete(RedisConstants.RECOMMEND_PRODUCT); // 删除缓存
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

        if (Objects.nonNull(query.getRecommend())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::isRecommend, query.getRecommend())
                    .orderByDesc(FinancialProduct::getRecommendWeight);
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
    public PurchaseResultDto purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order) {
        FinancialProduct product = this.getById(purchaseQuery.getProductId());
        BigDecimal amount = purchaseQuery.getAmount();

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
        accountBalanceServiceImpl.decrease(uid, ChargeType.purchase, product.getCoin(), amount, order.getOrderNo(), CurrencyLogDes.申购.name());

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toFinancialPurchaseResultVO(financialRecord);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setStatusDes(order.getStatus().name());
        financialPurchaseResultVO.setOrderNo(order.getOrderNo());
        return PurchaseResultDto.builder()
                .recordId(financialRecord.getId())
                .financialPurchaseResultVO(financialPurchaseResultVO).build();
    }

    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        var productId = product.getId();

        BigDecimal personUse = financialRecordService.getUseQuota(List.of(productId), uid).getOrDefault(productId, BigDecimal.ZERO);

        if (product.getPersonQuota() != null && product.getPersonQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(personUse).compareTo(product.getPersonQuota()) > 0) {
            ErrorCodeEnum.throwException("个人申购额度不足");
        }
    }

    @Override
    public List<Long> holdProductIds(Long uid) {
        return financialRecordService.getBaseMapper().holdProductIds(uid);
    }

    /**
     * 预计收益接口
     */
    public ExpectIncomeVO expectIncome(Long productId, BigDecimal amount) {
        FinancialProduct product = this.getById(productId);
        ExpectIncomeVO expectIncomeVO = new ExpectIncomeVO();

        if (Objects.isNull(product)) {
            expectIncomeVO.setExpectIncome(BigDecimal.ZERO);
            return expectIncomeVO;
        }


        if (product.getRateType() == 1) {
            BigDecimal income = financialProductLadderRateService.calLadderIncome(productId, amount);
            expectIncomeVO.setExpectIncome(income);
            return expectIncomeVO;
        }

        expectIncomeVO.setExpectIncome(product.getRate().multiply(amount)
                .multiply(BigDecimal.valueOf(product.getTerm().getDay()))
                .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN));


        return expectIncomeVO;
    }

    @Override
    public ExpectIncomeVO exceptDailyIncome(Long uid, Long productId, Long recordId) {
        FinancialRecord record = financialRecordService.selectById(recordId, uid);
        ExpectIncomeVO expectIncomeVO = this.expectIncome(productId, record.getHoldAmount());
        expectIncomeVO.setExpectIncome(expectIncomeVO.getExpectIncome()
                .divide(new BigDecimal(record.getProductTerm().getDay()), 8, RoundingMode.DOWN));
        return expectIncomeVO;
    }

    @Override
    public BigDecimal incomeRate(Long uid, Long productId, Long recordId) {
        FinancialIncomeAccrue financialIncomeAccrue = financialIncomeAccrueService.getByRecordId(uid, recordId);
        if (Objects.isNull(financialIncomeAccrue)) {
            return BigDecimal.ZERO;
        }
        // 累计收益
        BigDecimal incomeAmount = financialIncomeAccrue.getIncomeAmount();

        FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);

        List<Order> redeemOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, ChargeStatus.chain_success)
                .eq(Order::getType, ChargeType.redeem)
                .eq(Order::getUid, uid)
                .eq(Order::getRelatedId, recordId));

        BigDecimal allHoldAmount = financialRecord.getHoldAmount()
                .add(redeemOrders.stream().map(Order::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return incomeAmount.divide(allHoldAmount, 4, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public RedeemResultDto redeemOperation(Long uid, RedeemQuery query) {
        // todo 计算利息的时候不允许进行赎回操
        Long recordId = query.getRecordId();
        FinancialRecord record = financialRecordService.selectById(recordId, uid);

        if (RecordStatus.SUCCESS.equals(record.getStatus())) {
            log.info("recordId:{},已经处于完成状态，请校验是否有误", recordId);
            ErrorCodeEnum.TRADE_FAIL.throwException();
        }

        if (query.getRedeemAmount().compareTo(record.getHoldAmount()) > 0) {
            log.info("赎回金额 {}  大于持有金额 {}", query.getRedeemAmount(), record.getHoldAmount());
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        //创建赎回订单  没有审核操作，在一个事物里无需操作
        LocalDateTime now = LocalDateTime.now();
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setId(id);
        order.setUid(uid);
        order.setAmount(query.getRedeemAmount());
        order.setOrderNo(AccountChangeType.redeem.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.chain_success);
        order.setType(ChargeType.redeem);
        order.setRelatedId(recordId);
        order.setCoin(record.getCoin());
        order.setCreateTime(now);
        order.setCompleteTime(now);
        orderService.save(order);

        // 增加
        accountBalanceServiceImpl.increase(uid, ChargeType.redeem, record.getCoin(), query.getRedeemAmount(), order.getOrderNo(), CurrencyLogDes.赎回.name());

        // 减少产品持有
        financialRecordService.redeem(record.getId(), query.getRedeemAmount(), record.getHoldAmount());

        // 更新记录状态
        FinancialRecord recordLatest = financialRecordService.selectById(recordId, uid);
        if (recordLatest.getHoldAmount().compareTo(BigDecimal.ZERO) == 0) {
            recordLatest.setStatus(RecordStatus.SUCCESS);
            recordLatest.setUpdateTime(LocalDateTime.now());
            productHoldRecordService.delete(uid, record.getProductId(), record.getId());

        }
        financialRecordService.updateById(recordLatest);
        return RedeemResultDto.builder()
                .orderNo(order.getOrderNo()).build();
    }

    @Override
    public IncomeDto incomeOperation(Long uid, Long productId, Long recordId) {
        FinancialIncomeAccrue accrue = financialIncomeAccrueService.getByRecordId(uid, recordId);
        var record = financialRecordService.getById(recordId);
        return IncomeDto.builder()
                .coin(record.getCoin())
                .holdAmount(record.getHoldAmount())
                .accrueIncomeAmount(accrue == null ? BigDecimal.ZERO : accrue.getIncomeAmount())
                .dailyIncomeAmount(this.exceptDailyIncome(uid, productId, recordId).getExpectIncome())
                .calIncomeAmount(BigDecimal.ZERO)
                .waitIncomeAmount(BigDecimal.ZERO).build();
    }

}
