package com.tianli.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.tianli.financial.dto.ProductRateDTO;
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
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.vo.FinancialProductDropdownVO;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinancialServiceImpl implements FinancialService {

    @Override
    @Transactional
    public FinancialPurchaseResultVO purchase(Long uid, PurchaseQuery purchaseQuery) {
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());
        BigDecimal amount = purchaseQuery.getAmount();

        // 校验操作
        validProduct(product, amount);
        validRemainAmount(uid, purchaseQuery.getCoin(), amount);
        validPurchaseAmount(uid, product, amount);

        // 如果是活期，判断是否已经存在申购记录，如果有的话，额外添加待记息金额不生成新的记录
        FinancialRecord financialRecord;
        Optional<FinancialRecord> recordOptional = Optional.empty();
        if (ProductType.current.equals(product.getType())) {
            recordOptional = financialRecordService.selectByProductId(purchaseQuery.getProductId())
                    .stream()
                    .sorted(Comparator.comparing(FinancialRecord::getEndTime).reversed())
                    .filter(index -> RecordStatus.PROCESS.equals(index.getStatus())).findFirst();
        }
        // 如果存在申购记录，如果是当天继续申购，则累加金额，否则累加待记利息金额
        recordOptional.ifPresent(record -> financialRecordService.increaseWaitAmount(record.getId(), amount, record.getWaitAmount()));
        financialRecord = recordOptional.orElse(financialRecordService.generateFinancialRecord(uid, product, amount, purchaseQuery.isAutoCurrent()));

        // 生成一笔订单记录
        Order order = Order.builder()
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

        // 减少余额
        accountBalanceService.decrease(uid, ChargeType.purchase, product.getCoin(), amount, order.getOrderNo(), CurrencyLogDes.申购.name());
        // 增加已经使用申购额度
        financialProductService.increaseUseQuota(product.getId(), amount, product.getUseQuota());

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toFinancialPurchaseResultVO(financialRecord);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setStatusDes(order.getStatus().name());
        financialPurchaseResultVO.setOrderNo(order.getOrderNo());
        return financialPurchaseResultVO;
    }

    @Override
    public DollarIncomeVO income(Long uid) {
        List<ProductType> types = List.of(ProductType.values());

        BigDecimal totalHoldFeeDollar = BigDecimal.ZERO;
        BigDecimal totalAccrueIncomeFeeDollar = BigDecimal.ZERO;
        BigDecimal totalYesterdayIncomeFeeDollar = BigDecimal.ZERO;
        EnumMap<ProductType, DollarIncomeVO> incomeMap = new EnumMap<>(ProductType.class);
        for (ProductType type : types) {
            DollarIncomeVO incomeVO = new DollarIncomeVO();
            // 单类型产品持有币数量
            BigDecimal holdFeeDollar = financialRecordService.getPurchaseAmount(uid, type, RecordStatus.PROCESS);
            incomeVO.setHoldFee(holdFeeDollar);
            totalHoldFeeDollar = totalHoldFeeDollar.add(holdFeeDollar);

            // 单类型产品累计收益
            BigDecimal incomeAmountDollar = financialIncomeAccrueService.getAccrueDollarAmount(uid, type);
            incomeVO.setAccrueIncomeFee(incomeAmountDollar);
            totalAccrueIncomeFeeDollar = totalAccrueIncomeFeeDollar.add(incomeAmountDollar);

            // 单个类型产品昨日收益
            BigDecimal yesterdayIncomeAmountDollar = financialIncomeDailyService.getYesterdayDailyDollarAmount(uid, type);
            incomeVO.setYesterdayIncomeFee(yesterdayIncomeAmountDollar);
            totalYesterdayIncomeFeeDollar = totalYesterdayIncomeFeeDollar.add(yesterdayIncomeAmountDollar);

            incomeMap.put(type, incomeVO);
        }


        DollarIncomeVO incomeVO = new DollarIncomeVO();
        incomeVO.setHoldFee(totalHoldFeeDollar);
        incomeVO.setAccrueIncomeFee(totalAccrueIncomeFeeDollar);
        incomeVO.setYesterdayIncomeFee(totalYesterdayIncomeFeeDollar);
        incomeVO.setIncomeMap(incomeMap);

        return incomeVO;
    }

    @Override
    public RecordIncomeVO recordIncome(Long uid, Long recordId) {
        FinancialRecord record = financialRecordService.selectById(recordId, uid);
        RecordIncomeVO incomeByRecordIdVO = financialConverter.toIncomeByRecordIdVO(record);

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
    public IPage<HoldProductVo> holdProductPage(IPage<FinancialRecord> page, Long uid, ProductType type) {

        var financialRecords = financialRecordService.selectListPage(page, uid, type, RecordStatus.PROCESS);
        if (CollectionUtils.isEmpty(financialRecords.getRecords())) {
            return financialRecords.convert(financialRecord -> new HoldProductVo());
        }

        var recordIds = financialRecords.getRecords().stream().map(FinancialRecord::getId).collect(Collectors.toList());

        var accrueIncomeMap = financialIncomeAccrueService.selectListByRecordId(recordIds).stream()
                .collect(Collectors.toMap(FinancialIncomeAccrue::getRecordId, o -> o));
        var dailyIncomeMap = financialIncomeDailyService.selectListByRecordIds(uid, recordIds, requestInitService.yesterday()).stream()
                .collect(Collectors.toMap(FinancialIncomeDaily::getRecordId, o -> o));

        return financialRecords.convert(financialRecord -> {
            var holdProductVo = new HoldProductVo();
            var accrueIncomeLog = Optional.ofNullable(accrueIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeAccrue());
            var dailyIncomeLog = Optional.ofNullable(dailyIncomeMap.get(financialRecord.getId())).orElse(new FinancialIncomeDaily());

            holdProductVo.setRecordId(financialRecord.getId());
            holdProductVo.setName(financialRecord.getProductName());
            holdProductVo.setNameEn(financialRecord.getProductNameEn());
            holdProductVo.setRate(financialRecord.getRate());
            holdProductVo.setProductType(financialRecord.getProductType());
            holdProductVo.setRiskType(financialRecord.getRiskType());
            holdProductVo.setLogo(financialRecord.getLogo());
            holdProductVo.setCoin(financialRecord.getCoin());

            IncomeVO incomeVO = new IncomeVO();
            incomeVO.setHoldFee(financialRecord.getHoldAmount());
            incomeVO.setAccrueIncomeFee(Optional.ofNullable(accrueIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));
            incomeVO.setYesterdayIncomeFee(Optional.ofNullable(dailyIncomeLog.getIncomeAmount()).orElse(BigDecimal.ZERO));

            holdProductVo.setIncomeVO(incomeVO);
            return holdProductVo;
        });
    }

    @Override
    public IPage<FinancialIncomeDailyVO> dailyIncomePage(IPage<FinancialIncomeDaily> page, Long uid, Long recordId) {
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
        if (Objects.isNull(financialProduct)) {
            ErrorCodeEnum.throwException("产品不存在");
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
    }

    @Override
    public void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount) {
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid, currencyCoin);
        if (accountBalanceBalance.getRemain().compareTo(amount) < 0) {
            ErrorCodeEnum.throwException("可用余额不足");
        }
    }

    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        var productId = product.getId();

        BigDecimal personUse = financialRecordService.getUseQuota(List.of(productId), uid).getOrDefault(productId, BigDecimal.ZERO);

        if (product.getPersonQuota() != null && product.getPersonQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(personUse).compareTo(product.getPersonQuota()) > 0) {
            ErrorCodeEnum.throwException("个人申购额度不足");
        }

        if (product.getTotalQuota() != null && product.getTotalQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(product.getUseQuota()).compareTo(product.getTotalQuota()) > 0) {
            ErrorCodeEnum.throwException("总申购额度不足");
        }
    }

    @Override
    public IPage<OrderFinancialVO> orderPage(Page<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        return orderService.selectByPage(page, financialOrdersQuery);
    }

    @Override
    public IPage<FinancialIncomeAccrueDTO> incomeRecordPage(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return financialIncomeAccrueService.incomeRecord(page, query);
    }

    @Override
    public FinancialSummaryDataVO incomeSummaryData(FinancialProductIncomeQuery query) {
        return FinancialSummaryDataVO.builder()
                .incomeAmount(Optional.ofNullable(financialIncomeAccrueService.summaryIncomeByQuery(query)).orElse(BigDecimal.ZERO))
                .build();
    }

    @Override
    public IPage<RateScopeVO> summaryProducts(Page<FinancialProduct> page, ProductType productType) {

        IPage<ProductRateDTO> productRateDTOS = financialProductService.listProductRateDTO(page, productType);
        List<FinancialProductVO> financialProductVOs = getFinancialProductVOs(productType);
        var productMap = financialProductVOs.stream()
                .collect(Collectors.groupingBy(FinancialProductVO::getCoin, Collectors.toList()));

        return productRateDTOS.convert(productRateDTO -> {
            RateScopeVO financialProductRateVO = new RateScopeVO();
            financialProductRateVO.setCoin(productRateDTO.getCoin());
            financialProductRateVO.setLogo(productRateDTO.getCoin().getLogoPath());
            financialProductRateVO.setMaxRate(productRateDTO.getMaxRate());
            financialProductRateVO.setMinRate(productRateDTO.getMinRate());
            List<FinancialProductVO> productVOS = productMap.getOrDefault(productRateDTO.getCoin(), new ArrayList<>());
            financialProductRateVO.setProducts(productVOS);
            return financialProductRateVO;
        });

    }

    @Override
    public IPage<FinancialProductVO> products(Page<FinancialProduct> page, ProductType type) {

        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .orderByAsc(FinancialProduct::getType) // 活期优先
                .orderByDesc(FinancialProduct::getRate); // 年化利率降序

        return getFinancialProductVOIPage(page, type, query);

    }

    @Override
    public IPage<FinancialUserInfoVO> financialUserPage(String uid, IPage<Address> page) {

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
        var fixedAmount = financialRecordService.getSummaryAmount(uids, ProductType.fixed, RecordStatus.PROCESS);
        var currentAmount = financialRecordService.getSummaryAmount(uids, ProductType.current, RecordStatus.PROCESS);
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
    public FinancialSummaryDataVO userSummaryData(String uid) {
        IPage<Address> page = new Page<>(1, Integer.MAX_VALUE);
        var userInfos = financialUserPage(uid, page).getRecords();

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

    @Override
    @Transactional
    public void boardManual(TimeQuery query) {
        LocalDateTime time = query.getTime();
        LocalDateTime dateTime = TimeTool.minDay(time);
        FinancialBoardProduct financialBoardProduct =
                financialBoardProductService.getFinancialBoardProduct(dateTime, dateTime.plusDays(1), null);
        FinancialBoardWallet financialBoardWallet =
                financialBoardWalletService.getFinancialBoardWallet(dateTime, dateTime.plusDays(1), null);
        financialBoardProduct.setCreateTime(dateTime.toLocalDate());
        financialBoardWallet.setCreateTime(dateTime.toLocalDate());
        financialBoardProductService.update(financialBoardProduct, new LambdaQueryWrapper<FinancialBoardProduct>()
                .eq(FinancialBoardProduct::getCreateTime, dateTime));
        financialBoardWalletService.update(financialBoardWallet, new LambdaQueryWrapper<FinancialBoardWallet>()
                .eq(FinancialBoardWallet::getCreateTime, dateTime));
    }

    public CurrentProductPurchaseVO currentProductDetails(Long productId) {
        FinancialProductVO financialProductVO = getFinancialProductVOs(productId).get(0);
        CurrentProductPurchaseVO productVO = financialConverter.toFinancialProductDetailsVO(financialProductVO);

        if (productVO.getRateType() == 1) {
            productVO.setLadderRates(financialProductLadderRateService.listByProductId(productId)
                    .stream().map(financialConverter::toProductLadderRateVO).collect(Collectors.toList()));
        }

        return productVO;
    }

    @Override
    public FixedProductsPurchaseVO fixedProductDetails(CurrencyCoin coin) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getCoin, coin)
                .eq(FinancialProduct::getType, ProductType.fixed)
                .eq(FinancialProduct::getStatus, ProductStatus.open);
        IPage<FinancialProductVO> financialProductVOIPage =
                getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), null, query);

        List<FinancialProductVO> productVOS = financialProductVOIPage.getRecords()
                .stream().filter(FinancialProductVO::isAllowPurchase).collect(Collectors.toList());
        FixedProductsPurchaseVO fixedProductsPurchaseVO = new FixedProductsPurchaseVO();
        fixedProductsPurchaseVO.setProducts(productVOS);
        fixedProductsPurchaseVO.setTerms(productVOS.stream().map(FinancialProductVO::getTerm).collect(Collectors.toList()));
        return fixedProductsPurchaseVO;
    }

    @Override
    public List<FinancialProductDropdownVO> dropdownList(ProductType type) {
        List<FinancialProduct> financialProducts = financialProductService.list(new QueryWrapper<FinancialProduct>().lambda()
                .eq(FinancialProduct::getType, type)
                .eq(FinancialProduct::getStatus, ProductStatus.open));
        return financialProducts.stream().map(financialProduct ->
                        new FinancialProductDropdownVO(financialProduct.getId(), financialProduct.getName(), financialProduct.getNameEn()))
                .collect(Collectors.toList());
    }

    private List<FinancialProductVO> getFinancialProductVOs(Long productId) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getId, productId);
        return getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), null, query).getRecords();
    }

    private List<FinancialProductVO> getFinancialProductVOs(ProductType productType) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .eq(FinancialProduct::isDeleted, false);
        return getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), productType, query).getRecords();
    }

    private IPage<FinancialProductVO> getFinancialProductVOIPage(Page<FinancialProduct> page, ProductType type, LambdaQueryWrapper<FinancialProduct> query) {
        if (Objects.nonNull(type)) {
            query = query.eq(FinancialProduct::getType, type);
        }


        var list = financialProductService.page(page, query);
        List<Long> productIds = list.getRecords().stream().map(FinancialProduct::getId).distinct().collect(Collectors.toList());

        Boolean isNewUser = financialRecordService.isNewUser(requestInitService.uid());

        Map<Long,Long> firstProcessRecordMap = financialRecordService.firstProcessRecordMap(productIds, requestInitService.uid());
        Map<Long, BigDecimal> usePersonQuotaMap = financialRecordService.getUseQuota(productIds, requestInitService.uid());

        return list.convert(product -> {
            BigDecimal usePersonQuota = usePersonQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal totalQuota = product.getTotalQuota();
            BigDecimal personQuota = product.getPersonQuota();
            BigDecimal useQuota = product.getUseQuota();

            // 设置额度信息
            FinancialProductVO financialProductVO = financialConverter.toFinancialProductVO(product);
            financialProductVO.setUserPersonQuota(usePersonQuota);
            financialProductVO.setHoldAmount(usePersonQuota);
            // 设置是否可以申购
            boolean allowPurchase =
                    checkAllowPurchase(usePersonQuota, useQuota, personQuota, totalQuota, product.getBusinessType(), isNewUser);
            financialProductVO.setAllowPurchase(allowPurchase);

            // 设置假数据
            BigDecimal baseDataAmount = getBaseDataAmount(product.getId(), totalQuota, useQuota);
            if (Objects.nonNull(baseDataAmount)) {
                financialProductVO.setUseQuota(useQuota.add(baseDataAmount));
                financialProductVO.setBaseUseQuota(baseDataAmount);
            }

            LocalDateTime startIncomeTime = LocalDateTime.now().plusDays(product.getTerm().getDay());
            financialProductVO.setStartIncomeTime(startIncomeTime);
            financialProductVO.setSettleTime(startIncomeTime.plusDays(product.getTerm().getDay()));

            // 设置第一个有效可数据的record id
            financialProductVO.setRecordId(firstProcessRecordMap.get(product.getId()));

            return financialProductVO;
        });
    }

    /**
     * 判断是否可以申购
     *
     * @param usePersonQuota 个人使用额度
     * @param useQuota       总使用额度
     * @param personQuota    限度个人额度
     * @param totalQuota     限定使用额度
     * @return true or false
     */
    private boolean checkAllowPurchase(BigDecimal usePersonQuota,
                                       BigDecimal useQuota,
                                       BigDecimal personQuota,
                                       BigDecimal totalQuota,
                                       BusinessType businessType,
                                       boolean isNewUser) {

        // 如果是新用户福利产品，仅限新用户
        if (BusinessType.benefits.equals(businessType) && !isNewUser) {
            return false;
        }

        // 如果个人额度和总额度都不存在，直接可以购买
        if (Objects.isNull(personQuota) && Objects.isNull(totalQuota)) {
            return true;
        }

        // 如果存在个人额度不存在总额度 比较个人额度
        if (Objects.nonNull(personQuota) && Objects.isNull(totalQuota)) {
            return usePersonQuota.compareTo(personQuota) < 0;
        }

        // 如果存在总额度不存在个人额度 比较总额度
        if (Objects.isNull(personQuota)) {
            return useQuota.compareTo(totalQuota) < 0;
        }

        // 如果个人额度和总额度都存在 比较两者
        return usePersonQuota.compareTo(personQuota) < 0 && useQuota.compareTo(totalQuota) < 0;

    }

    /**
     * 获取假数据基础数据
     */
    private BigDecimal getBaseDataAmount(Long productId, BigDecimal limitQuota, BigDecimal useQuota) {
        if (Objects.isNull(limitQuota) || BigDecimal.ZERO.compareTo(limitQuota) == 0) {
            return null;
        }
        useQuota = Optional.ofNullable(useQuota).orElse(BigDecimal.ZERO);
        // 实际比例
        BigDecimal realRate = useQuota.divide(limitQuota, 4, RoundingMode.HALF_UP);
        // 期望比例
        BigDecimal expectRate = BigDecimal.valueOf(0.5f);
        var baseRate = BigDecimal.valueOf(productId % 4).multiply(BigDecimal.valueOf(0.05f));
        if (realRate.compareTo(expectRate) >= 0) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        int dayOfMonth = now.getDayOfMonth();
        int month = now.getMonthValue();
        String monthAndDay = now.format(DateTimeFormatter.ofPattern("MMdd"));
        int randomNum = (Integer.parseInt(monthAndDay) + dayOfMonth * 100 + month * 100) % 365;
        if (randomNum < 300) {
            randomNum = randomNum + 50;
        }
        // 获取一个每天固定的随机比例 365 以内
        BigDecimal randomRate = BigDecimal.valueOf(randomNum).divide(BigDecimal.valueOf(365L), 4, RoundingMode.HALF_DOWN);

        // 调整比例 = 差值比例 - 差值比例 * 0.2 * 每天固定随机比例
        BigDecimal adjustRate = expectRate.multiply(BigDecimal.valueOf(0.6f).multiply(randomRate).add(baseRate));

        return limitQuota.multiply(adjustRate);
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
    @Resource
    private FinancialBoardProductService financialBoardProductService;
    @Resource
    private FinancialBoardWalletService financialBoardWalletService;
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;

}
