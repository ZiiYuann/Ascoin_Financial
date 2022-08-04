package com.tianli.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.RedisService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.*;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinancialServiceImpl implements FinancialService {

    @Override
    @Transactional
    public FinancialPurchaseResultVO purchase(PurchaseQuery purchaseQuery) {
        // 如果产品处于要下线的情况，不允许购买
        Long productId = purchaseQuery.getProductId();
        boolean exists = redisService.exists(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + productId);
        if (exists) {
            ErrorCodeEnum.PRODUCT_CAN_NOT_BUY.throwException();
        }

        Long uid = requestInitService.uid();

        FinancialProduct product = financialProductService.getById(productId);
        validProduct(product, purchaseQuery.getAmount());

        BigDecimal amount = purchaseQuery.getAmount();
        validRemainAmount(uid, purchaseQuery.getCoin(), amount);


        BigDecimal totalUse = financialRecordService.getUseQuota(List.of(productId)).getOrDefault(productId, BigDecimal.ZERO);
        BigDecimal personUse = financialRecordService.getUseQuota(List.of(productId), uid).getOrDefault(productId, BigDecimal.ZERO);


        if(product.getPersonQuota() != null &&  product.getPersonQuota().compareTo(BigDecimal.ZERO) >0 &&
                purchaseQuery.getAmount().add(personUse).compareTo(product.getPersonQuota()) > 0){
            ErrorCodeEnum.throwException("用户申购金额超过个人限额");
        }

        if(product.getTotalQuota() != null &&  product.getTotalQuota().compareTo(BigDecimal.ZERO) >0 &&
                purchaseQuery.getAmount().add(totalUse).compareTo(product.getTotalQuota()) > 0){
            ErrorCodeEnum.throwException("用户申购金额超过总限购额");
        }


        // 生成一笔订单记录(进行中)
        Order order = Order.builder()
                .uid(uid)
                .coin(product.getCoin())
                .relatedId(null)
                .orderNo(AccountChangeType.purchase.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(amount)
                .type(ChargeType.purchase)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .completeTime(LocalDateTime.now())
                .build();
        orderService.save(order);
        // 减少余额
        accountBalanceService.withdraw(uid, ChargeType.purchase, product.getCoin(), amount, order.getOrderNo(), CurrencyLogDes.申购.name());
        // 确认完毕后生成申购记录
        FinancialRecord financialRecord = financialRecordService.generateFinancialRecord(uid, product, amount,purchaseQuery.isAutoCurrent());
        // 修改订单状态
        order.setStatus(ChargeStatus.chain_success);
        order.setRelatedId(financialRecord.getId());
        orderService.saveOrUpdate(order);

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toFinancialPurchaseResultVO(financialRecord);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setStatusDes(order.getStatus().name());
        financialPurchaseResultVO.setOrderNo(order.getOrderNo());
        return financialPurchaseResultVO;
    }

    @Override
    public IncomeVO income(Long uid) {
        List<ProductType> types = List.of(ProductType.values());

        BigDecimal totalHoldFee = BigDecimal.ZERO;
        BigDecimal totalAccrueIncomeFee = BigDecimal.ZERO;
        BigDecimal totalYesterdayIncomeFee = BigDecimal.ZERO;
        EnumMap<ProductType, IncomeVO> incomeMap = new EnumMap<>(ProductType.class);
        for (ProductType type : types) {
            IncomeVO incomeVO = new IncomeVO();
            // 单类型产品持有币数量
            BigDecimal holdFee = financialRecordService.getPurchaseAmount(uid, type, RecordStatus.PROCESS);
            incomeVO.setHoldFee(holdFee);
            totalHoldFee = totalHoldFee.add(holdFee);

            // 单类型产品累计收益
            BigDecimal incomeAmount = financialIncomeAccrueService.getAccrueAmount(uid, type);
            incomeVO.setAccrueIncomeFee(incomeAmount);
            totalAccrueIncomeFee = totalAccrueIncomeFee.add(incomeAmount);

            // 单个类型产品昨日收益
            BigDecimal yesterdayIncomeFee = financialIncomeDailyService.getYesterdayDailyAmount(uid, type);
            incomeVO.setYesterdayIncomeFee(yesterdayIncomeFee);
            totalYesterdayIncomeFee = totalYesterdayIncomeFee.add(yesterdayIncomeFee);

            incomeMap.put(type, incomeVO);
        }


        IncomeVO incomeVO = new IncomeVO();
        incomeVO.setHoldFee(totalHoldFee);
        incomeVO.setAccrueIncomeFee(totalAccrueIncomeFee);
        incomeVO.setYesterdayIncomeFee(totalYesterdayIncomeFee);
        incomeVO.setIncomeMap(incomeMap);

        return incomeVO;
    }

    @Override
    public IncomeByRecordIdVO incomeByRecordId(Long uid, Long recordId) {
        FinancialRecord record = financialRecordService.selectById(recordId, uid);
        IncomeByRecordIdVO incomeByRecordIdVO = financialConverter.toIncomeByRecordIdVO(record);

        LambdaQueryWrapper<FinancialIncomeDaily> incomeDailyQuery = new LambdaQueryWrapper<FinancialIncomeDaily>().eq(FinancialIncomeDaily::getUid, uid)
                .eq(FinancialIncomeDaily::getRecordId, recordId)
                .eq(FinancialIncomeDaily::getFinishTime, DateUtil.beginOfDay(new Date()).toLocalDateTime().plusDays(-1));
        var yesterdayIncomeFee = Optional.ofNullable(financialIncomeDailyService.list(incomeDailyQuery)).orElse(new ArrayList<>())
                .stream().map(FinancialIncomeDaily::getIncomeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<FinancialIncomeAccrue> incomeAccrueQuery = new LambdaQueryWrapper<FinancialIncomeAccrue>().eq(FinancialIncomeAccrue::getUid, uid)
                .eq(FinancialIncomeAccrue::getRecordId, recordId);
        FinancialIncomeAccrue incomeAccrue = Optional.ofNullable(financialIncomeAccrueService.getOne(incomeAccrueQuery))
                .orElse(new FinancialIncomeAccrue());

        incomeByRecordIdVO.setAccrueIncomeFee(Optional.ofNullable(incomeAccrue.getIncomeAmount()).orElse(BigDecimal.ZERO));
        incomeByRecordIdVO.setYesterdayIncomeFee(yesterdayIncomeFee);
        incomeByRecordIdVO.setRecordStatus(record.getStatus());
        incomeByRecordIdVO.setAutoRenewal(record.isAutoRenewal());
        incomeByRecordIdVO.setProductId(record.getProductId());

        return incomeByRecordIdVO;
    }

    @Override
    public IPage<HoldProductVo> myHold(IPage<FinancialRecord> page, Long uid, ProductType type) {

        var financialRecords = financialRecordService.selectListPage(page, uid, type, RecordStatus.PROCESS);
        if (CollectionUtils.isEmpty(financialRecords.getRecords())) {
            return financialRecords.convert(financialRecord -> new HoldProductVo());
        }

        var productIds = financialRecords.getRecords().stream().map(FinancialRecord::getProductId).collect(Collectors.toList());
        var recordIds = financialRecords.getRecords().stream().map(FinancialRecord::getId).collect(Collectors.toList());

        var productMap = financialProductService.listByIds(productIds).stream()
                .collect(Collectors.toMap(FinancialProduct::getId, o -> o));
        var accrueIncomeMap = financialIncomeAccrueService.selectListByRecordId(recordIds).stream()
                .collect(Collectors.toMap(FinancialIncomeAccrue::getRecordId, o -> o));
        var dailyIncomeMap = financialIncomeDailyService.selectListByRecordIds(uid, recordIds, requestInitService.yesterday()).stream()
                .collect(Collectors.toMap(FinancialIncomeDaily::getRecordId, o -> o));

        return financialRecords.convert(financialRecord -> {
            var holdProductVo = new HoldProductVo();
            var product = productMap.get(financialRecord.getProductId());
            var accrueIncomeLog = Optional.ofNullable(accrueIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeAccrue());
            var dailyIncomeLog = Optional.ofNullable(dailyIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeDaily());

            holdProductVo.setRecordId(financialRecord.getId());
            holdProductVo.setName(financialRecord.getProductName());
            holdProductVo.setNameEn(financialRecord.getProductNameEn());
            holdProductVo.setRate(product.getRate());
            holdProductVo.setProductType(product.getType());
            holdProductVo.setRiskType(product.getRiskType());
            holdProductVo.setLogo(financialRecord.getLogo());
            holdProductVo.setCoin(product.getCoin());

            IncomeVO incomeVO = new IncomeVO();
            incomeVO.setHoldFee(financialRecord.getHoldAmount());
            incomeVO.setAccrueIncomeFee(Optional.ofNullable(accrueIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));
            incomeVO.setYesterdayIncomeFee(Optional.ofNullable(dailyIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));

            holdProductVo.setIncomeVO(incomeVO);
            return holdProductVo;
        });
    }

    @Override
    public IPage<FinancialIncomeDailyVO> incomeDetails(IPage<FinancialIncomeDaily> page, Long uid, Long recordId) {
        FinancialRecord financialRecord = financialRecordService.selectById(recordId, uid);
        var dailyIncomeLogs = financialIncomeDailyService.pageByRecordId(page, uid, List.of(recordId), null);
        return dailyIncomeLogs.convert(income -> {
            FinancialIncomeDailyVO financialIncomeDailyVO = FinancialIncomeDailyVO.toVO(income);
            financialIncomeDailyVO.setCoin(financialRecord.getCoin());
            return financialIncomeDailyVO;
        });
    }

    @Override
    public void validProduct(FinancialProduct financialProduct, BigDecimal purchaseAmount) {
        if (Objects.isNull(financialProduct) || ProductStatus.open != financialProduct.getStatus()) {
            ErrorCodeEnum.NOT_OPEN.throwException();
        }

        if (purchaseAmount.compareTo(financialProduct.getLimitPurchaseQuota()) < 0) {
            throw ErrorCodeEnum.PURCHASE_AMOUNT_TO_SMALL.generalException("最小金额为:" + financialProduct.getLimitPurchaseQuota());
        }
    }

    @Override
    public void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount) {
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid, currencyCoin);
        if (accountBalanceBalance.getRemain().compareTo(amount) < 0) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    @Override
    public IPage<OrderFinancialVO> orderPage(Page<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        return orderService.selectByPage(page, financialOrdersQuery);
    }

    @Override
    public IPage<FinancialIncomeAccrueDTO> incomeRecord(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return financialIncomeAccrueService.incomeRecord(page, query);
    }

    @Override
    public FinancialSummaryDataVO summaryIncomeByQuery(FinancialProductIncomeQuery query) {
        return FinancialSummaryDataVO.builder()
                .incomeAmount(Optional.ofNullable(financialIncomeAccrueService.summaryIncomeByQuery(query)).orElse(BigDecimal.ZERO))
                .build();
    }

    @Override
    public IPage<FinancialProductVO> products(Page<FinancialProduct> page, ProductType type) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .orderByDesc(FinancialProduct::getRate);

        return getFinancialProductVOIPage(page, type, query);

    }

    @Override
    public IPage<FinancialProductVO> activitiesProducts(Page<FinancialProduct> page, BusinessType type) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getBusinessType, ProductStatus.open);
        return getFinancialProductVOIPage(page, null, query);
    }

    @Override
    public IPage<FinancialUserInfoVO> user(String uid, IPage<Address> page) {

        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(uid)) {
            queryWrapper = queryWrapper.like(Address::getUid, uid);
        }

        var addresses = addressService.page(page, queryWrapper);
        List<Long> uids = addresses.getRecords().stream().map(Address::getUid).collect(Collectors.toList());

        var summaryBalanceAmount = accountBalanceService.getSummaryBalanceAmount(uids);
        var rechargeOrderAmount = orderService.getSummaryOrderAmount(uids, ChargeType.recharge);
        var withdrawBalanceAmount = orderService.getSummaryOrderAmount(uids, ChargeType.withdraw);
        var moneyAmount = financialRecordService.getSummaryAmount(uids, null, RecordStatus.PROCESS);
        var fixedAmount = financialRecordService.getSummaryAmount(uids, ProductType.fixed, null);
        var currentAmount = financialRecordService.getSummaryAmount(uids, ProductType.current, null);
        var profitAndLossAmount = financialIncomeAccrueService.getSummaryAmount(uids);

        return addresses.convert(address -> {
            Long uid1 = address.getUid();
            return FinancialUserInfoVO.builder()
                    .uid(uid1)
                    .fixedAmount(fixedAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .balanceAmount(summaryBalanceAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .rechargeAmount(rechargeOrderAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .withdrawAmount(withdrawBalanceAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .moneyAmount(moneyAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .currentAmount(currentAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .fixedAmount(fixedAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .profitAndLossAmount(profitAndLossAmount.getOrDefault(uid1, BigDecimal.ZERO))
                    .build();
        });
    }

    @Override
    public FinancialSummaryDataVO userData(String uid) {
        IPage<Address> page = new Page<>(1, Integer.MAX_VALUE);
        var userInfos = user(uid, page).getRecords();

        BigDecimal rechargeAmount = BigDecimal.ZERO;
        BigDecimal withdrawAmount = BigDecimal.ZERO;
        BigDecimal moneyAmount = BigDecimal.ZERO;
        BigDecimal incomeAmount = BigDecimal.ZERO;

        for (FinancialUserInfoVO financialUserInfoVO : userInfos) {
            rechargeAmount = rechargeAmount.add(financialUserInfoVO.getRechargeAmount());
            withdrawAmount = withdrawAmount.add(financialUserInfoVO.getWithdrawAmount());
            moneyAmount = moneyAmount.add(financialUserInfoVO.getMoneyAmount());
            incomeAmount = incomeAmount.add(financialUserInfoVO.getProfitAndLossAmount());
        }
        return FinancialSummaryDataVO.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .moneyAmount(moneyAmount)
                .incomeAmount(incomeAmount)
                .build();
    }

    private IPage<FinancialProductVO> getFinancialProductVOIPage(Page<FinancialProduct> page, ProductType type, LambdaQueryWrapper<FinancialProduct> query) {
        if (Objects.nonNull(type)) {
            query.eq(FinancialProduct::getType, type);
        }


        var list = financialProductService.page(page, query);
        List<Long> productId = list.getRecords().stream().map(FinancialProduct::getId).distinct().collect(Collectors.toList());

        Map<Long, BigDecimal> useQuotaMap = financialRecordService.getUseQuota(productId);
        Map<Long, BigDecimal> usePersonQuotaMap = financialRecordService.getUseQuota(productId, requestInitService.uid());
        return list.convert(product -> {
            BigDecimal useQuota = useQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal usePersonQuota = usePersonQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);

            FinancialProductVO financialProductVO = financialConverter.toFinancialProductVO(product);
            financialProductVO.setUseQuota(useQuota);
            financialProductVO.setUserPersonQuota(usePersonQuota);
            if (Objects.isNull(product.getPersonQuota()) && Objects.isNull(product.getTotalQuota())) {
                financialProductVO.setAllowPurchase(true);
            }

            if (Objects.nonNull(product.getPersonQuota()) && Objects.nonNull(product.getTotalQuota())) {
                financialProductVO.setAllowPurchase(usePersonQuota.compareTo(product.getPersonQuota()) < 0 && useQuota.compareTo(product.getTotalQuota()) < 0);
            }

            if (Objects.nonNull(product.getPersonQuota()) && Objects.isNull(product.getTotalQuota())) {
                financialProductVO.setAllowPurchase(usePersonQuota.compareTo(product.getPersonQuota()) < 0);
            }
            if (Objects.isNull(product.getPersonQuota()) && Objects.nonNull(product.getTotalQuota())) {
                financialProductVO.setAllowPurchase(useQuota.compareTo(product.getTotalQuota()) < 0);
            }

            return financialProductVO;
        });
    }

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private FinancialIncomeDailyService financialIncomeDailyService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;
    @Resource
    private OrderService orderService;
    @Resource
    private AddressService addressService;
    @Resource
    private RedisService redisService;

}
