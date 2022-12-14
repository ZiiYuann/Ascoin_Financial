package com.tianli.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.MoreObjects;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisService;
import com.tianli.currency.service.CurrencyService;
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
import com.tianli.financial.mapper.ProductMapper;
import com.tianli.financial.service.*;
import com.tianli.financial.vo.*;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.management.vo.FundProductBindDropdownVO;
import com.tianli.management.vo.UserAmountDetailsVO;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinancialServiceImpl implements FinancialService {


    @Override
    public DollarIncomeVO income(Long uid) {
        List<ProductType> types = List.of(ProductType.values());

        BigDecimal totalHoldFeeDollar = BigDecimal.ZERO;
        BigDecimal totalAccrueIncomeFeeDollar = BigDecimal.ZERO;
        BigDecimal totalYesterdayIncomeFeeDollar = BigDecimal.ZERO;
        EnumMap<ProductType, DollarIncomeVO> incomeMap = new EnumMap<>(ProductType.class);

        // 理财数据
        for (ProductType type : types) {
            DollarIncomeVO incomeVO = new DollarIncomeVO();
            // 单类型产品持有币数量
            BigDecimal holdFeeDollar = financialRecordService.getPurchaseAmount(uid, type, RecordStatus.PROCESS)
                    .setScale(2, RoundingMode.DOWN);
            incomeVO.setHoldFee(holdFeeDollar);
            totalHoldFeeDollar = totalHoldFeeDollar.add(holdFeeDollar);

            // 单类型产品累计收益
            BigDecimal incomeAmountDollar = financialIncomeAccrueService.getAccrueDollarAmount(uid, type)
                    .setScale(2, RoundingMode.DOWN);
            incomeVO.setAccrueIncomeFee(incomeAmountDollar);
            totalAccrueIncomeFeeDollar = totalAccrueIncomeFeeDollar.add(incomeAmountDollar);

            // 单个类型产品昨日收益
            BigDecimal yesterdayIncomeAmountDollar = financialIncomeDailyService.amountDollarYesterday(uid, type)
                    .setScale(2, RoundingMode.DOWN);
            incomeVO.setYesterdayIncomeFee(yesterdayIncomeAmountDollar);
            totalYesterdayIncomeFeeDollar = totalYesterdayIncomeFeeDollar.add(yesterdayIncomeAmountDollar);

            incomeMap.put(type, incomeVO);
        }

        // 基金数据
        BigDecimal fundHoldDollarAmount = fundRecordService.dollarHold(uid, null, null)
                .setScale(2, RoundingMode.DOWN);
        BigDecimal fundTotalIncome = fundIncomeRecordService.amountDollar(uid, FundIncomeStatus.audit_success, null, null)
                .setScale(2, RoundingMode.DOWN);
        LocalDateTime time = LocalDateTime.now().plusDays(-1);
        BigDecimal fundYesterdayIncome = fundIncomeRecordService.amountDollar(uid, FundIncomeStatus.audit_success, TimeTool.minDay(time), TimeTool.maxDay(time))
                .setScale(2, RoundingMode.DOWN);

        DollarIncomeVO fundIncomeVo = new DollarIncomeVO();
        fundIncomeVo.setAccrueIncomeFee(fundTotalIncome);
        fundIncomeVo.setHoldFee(fundHoldDollarAmount);
        fundIncomeVo.setYesterdayIncomeFee(fundYesterdayIncome);
        incomeMap.put(ProductType.fund, fundIncomeVo);

        totalHoldFeeDollar = totalHoldFeeDollar.add(fundHoldDollarAmount);
        totalAccrueIncomeFeeDollar = totalAccrueIncomeFeeDollar.add(fundTotalIncome);
        totalYesterdayIncomeFeeDollar = totalYesterdayIncomeFeeDollar.add(fundYesterdayIncome);


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
        FinancialProduct product = financialProductService.getById(record.getProductId());

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
        incomeByRecordIdVO.setMaxRate(product.getMaxRate());
        incomeByRecordIdVO.setMinRate(product.getMinRate());
        incomeByRecordIdVO.setRateType(product.getRateType());
        incomeByRecordIdVO.setRate(product.getRate());
        if (Objects.nonNull(product.getTotalQuota())) {
            incomeByRecordIdVO.setSellOut(MoreObjects.firstNonNull(product.getUseQuota(), BigDecimal.ZERO)
                    .compareTo(product.getTotalQuota()) >= 0);
        }

        return incomeByRecordIdVO;
    }

    @Override
    public IPage<HoldProductVo> holdProductPage(IPage<FinancialProduct> page, Long uid, ProductType type) {

        IPage<HoldProductVo> holdProductVoPage = productMapper.holdProductPage(page, uid, Objects.isNull(type) ? null : type.name());
        holdProductVoPage.convert(holdProductVo -> {

            IncomeVO incomeVO = new IncomeVO();
            BigDecimal dollarRate = currencyService.getDollarRate(holdProductVo.getCoin());
            incomeVO.setHoldFee(dollarRate.multiply(holdProductVo.getHoldAmount()));
            incomeVO.setAccrueIncomeFee(dollarRate.multiply(Objects.isNull(holdProductVo.getAccrueIncomeAmount())
                    ? BigDecimal.ZERO : holdProductVo.getAccrueIncomeAmount()));
            if (ProductType.fund.equals(holdProductVo.getProductType())) {
                FundIncomeQuery query = new FundIncomeQuery();
                query.setUid(uid);
                query.setStatus(List.of(FundIncomeStatus.calculated, FundIncomeStatus.wait_audit));
                query.setFundId(holdProductVo.getRecordId());
                BigDecimal waitInterestAmount = fundIncomeRecordService.getAmount(query).stream().map(FundIncomeAmountDTO::getWaitInterestAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                incomeVO.setWaitAuditIncomeAmount(waitInterestAmount);
            }
            if (!ProductType.fund.equals(holdProductVo.getProductType())) {
                incomeVO.setYesterdayIncomeAmount(financialIncomeDailyService.amountYesterday(holdProductVo.getRecordId()));
            }

            holdProductVo.setIncomeVO(incomeVO);
            return holdProductVo;
        });
        return holdProductVoPage;
    }

    @Override
    public IPage<?> holdProduct(IPage<FinancialProduct> page, Long uid, ProductType type) {
        IPage<Long> holdProductIdsPage = productMapper.holdProductIds(page, uid, Objects.isNull(type) ? null : type.name());
        List<Long> productIds = new ArrayList<>(holdProductIdsPage.getRecords());

        var holdProductVoMap = productMapper.holdProducts(uid, productIds)
                .stream().collect(Collectors.groupingBy(HoldProductVo::getProductId));

        return holdProductIdsPage.convert(productId -> {
            HashMap<String, Object> index = new HashMap<>();
            index.put("product", financialProductService.getById(productId));
            index.put("holdProducts", holdProductVoMap.get(productId));
            return index;
        });
    }

    @Override
    public IPage<TransactionRecordVO> transactionRecordPage(IPage<FinancialProduct> page, Long uid, ProductType type) {
        return productMapper.transactionRecordPage(page, uid, Objects.isNull(type) ? null : type.name());
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
            CoinBase coinBase = coinBaseService.getByName(productRateDTO.getCoin());
            financialProductRateVO.setCoin(productRateDTO.getCoin());
            financialProductRateVO.setLogo(coinBase.getLogo());
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

        IPage<FinancialProductVO> financialProductVOIPage = getFinancialProductVOIPage(page, type, query);
        List<FinancialProductVO> records = financialProductVOIPage.getRecords();

        records.sort((a, b) -> {
            BigDecimal hold1 = MoreObjects.firstNonNull(a.getHoldAmount(), BigDecimal.ZERO);
            BigDecimal hold2 = MoreObjects.firstNonNull(b.getHoldAmount(), BigDecimal.ZERO);
            return -hold1.compareTo(hold2);
        });

        return financialProductVOIPage;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RecommendProductVO> recommendProducts() {

        Object o = redisService.get(RedisConstants.RECOMMEND_PRODUCT);
        if (Objects.nonNull(o)) {
            return (List<RecommendProductVO>) o;
        }

        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .eq(FinancialProduct::isRecommend, true)
                .orderByDesc(FinancialProduct::getRate); // 年化利率降序

        List<RecommendProductVO> result = financialProductService.list(query)
                .stream().map(financialConverter::toRecommendProductVO)
                .collect(Collectors.toList());

        redisService.set(RedisConstants.RECOMMEND_PRODUCT, result, 1L, TimeUnit.DAYS);
        return result;
    }

    @Override
    public IPage<FinancialUserInfoVO> financialUserPage(String uid, IPage<Address> page) {

        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(uid)) {
            queryWrapper = queryWrapper.like(Address::getUid, uid);
        }

        var addresses = addressService.page(page, queryWrapper);
        List<Long> uids = addresses.getRecords().stream().map(Address::getUid).collect(Collectors.toList());

        var summaryBalanceAmount = accountBalanceServiceImpl.getSummaryBalanceAmount(uids);
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
        BigDecimal rechargeAmount = BigDecimal.ZERO;
        BigDecimal withdrawAmount = BigDecimal.ZERO;
        BigDecimal moneyAmount = BigDecimal.ZERO;
        BigDecimal incomeAmount = BigDecimal.ZERO;

        if (StringUtils.isNotBlank(uid)) {
            IPage<Address> page = new Page<>(1, 10);
            var userInfos = financialUserPage(uid, page).getRecords();


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

        FinancialChargeQuery query = new FinancialChargeQuery();

        // 充值
        query.setChargeType(ChargeType.recharge);
        rechargeAmount = orderService.orderAmountDollarSum(query);
        // 提现
        query.setChargeType(ChargeType.withdraw);
        withdrawAmount = orderService.orderAmountDollarSum(query);
        // 持有数量
        BigDecimal currentAmount = financialRecordService.dollarHold(ProductType.current);
        BigDecimal fixedAmount = financialRecordService.dollarHold(ProductType.fixed);
        moneyAmount = moneyAmount.add(currentAmount).add(fixedAmount);
        // 累计盈亏
        incomeAmount = financialIncomeAccrueService.summaryIncomeByQuery(new FinancialProductIncomeQuery());

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
    public FixedProductsPurchaseVO fixedProductDetails(String coin) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getCoin, coin)
                .eq(FinancialProduct::getType, ProductType.fixed)
                .eq(FinancialProduct::getStatus, ProductStatus.open);
        IPage<FinancialProductVO> financialProductVOIPage =
                getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), null, query);

        List<FinancialProductVO> productVOS = financialProductVOIPage.getRecords();
        FixedProductsPurchaseVO fixedProductsPurchaseVO = new FixedProductsPurchaseVO();
        fixedProductsPurchaseVO.setProducts(productVOS);
        fixedProductsPurchaseVO.setTerms(productVOS.stream().map(FinancialProductVO::getTerm).collect(Collectors.toList()));
        return fixedProductsPurchaseVO;
    }

    @Override
    public List<FundProductBindDropdownVO> fundProductBindDropdownList(ProductType type) {

        List<Long> bindProductIds = walletAgentProductService.listProductIdExcludeAgentId(null);

        LambdaQueryWrapper<FinancialProduct> queryWrapper = new QueryWrapper<FinancialProduct>().lambda()
                .eq(FinancialProduct::getType, type)
                .eq(FinancialProduct::getStatus, ProductStatus.close)
                .eq(FinancialProduct::isDeleted, false);


        if (CollectionUtils.isNotEmpty(bindProductIds)) {
            queryWrapper = queryWrapper.notIn(FinancialProduct::getId, bindProductIds);
        }


        List<FinancialProduct> financialProducts = financialProductService.list(queryWrapper);
        return financialProducts.stream()
                .map(financialProduct ->
                        new FundProductBindDropdownVO(financialProduct.getId(), financialProduct.getName(), financialProduct.getNameEn()))
                .collect(Collectors.toList());
    }

    @Override
    public UserAmountDetailsVO userAmountDetailsVO(Long uid) {
        BigDecimal dollarBalance = accountBalanceServiceImpl.dollarBalance(uid);
        BigDecimal dollarRecharge = orderService.uAmount(uid, ChargeType.recharge);
        BigDecimal dollarWithdraw = orderService.uAmount(uid, ChargeType.withdraw);
        FundRecordQuery fundRecordQuery = new FundRecordQuery();
        fundRecordQuery.setUid(uid);
        BigDecimal dollarFund = fundRecordService.dollarHold(fundRecordQuery);
        BigDecimal dollarFinancial = financialRecordService.dollarHold(uid);

        BigDecimal dollarFinancialIncome = financialIncomeAccrueService.getAccrueDollarAmount(uid, null);
        FundIncomeQuery fundIncomeQuery = new FundIncomeQuery();
        fundIncomeQuery.setUid(uid);
        fundIncomeQuery.setStatus(List.of(FundIncomeStatus.audit_success));
        BigDecimal dollarFundIncome = fundIncomeRecordService.amountDollar(fundIncomeQuery);

        return UserAmountDetailsVO.builder()
                .dollarBalance(dollarBalance)
                .dollarRecharge(dollarRecharge)
                .dollarWithdraw(dollarWithdraw)
                .dollarFund(dollarFund)
                .dollarFinancial(dollarFinancial)
                .dollarFinancialIncome(dollarFinancialIncome)
                .dollarFundIncome(dollarFundIncome)
                .build();
    }

    @Override
    public ProductInfoVO productExtraInfo(Long uid, Long productId) {
        Boolean newUser = financialRecordService.isNewUser(requestInitService.uid());
        FinancialProduct product = financialProductService.getById(productId);

        return ProductInfoVO.builder()
                .productId(productId)
                .sellOut(!Objects.isNull(product.getTotalQuota())
                        && product.getTotalQuota().compareTo(MoreObjects.firstNonNull(product.getUseQuota(), BigDecimal.ZERO)) >= 0)
                .businessType(product.getBusinessType())
                .newUser(newUser)
                .type(product.getType()).build();
    }

    private List<FinancialProductVO> getFinancialProductVOs(Long productId) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getId, productId);
        return getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), null, query).getRecords();
    }

    private List<FinancialProductVO> getFinancialProductVOs(ProductType productType) {
        LambdaQueryWrapper<FinancialProduct> query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .eq(FinancialProduct::isDeleted, false)
                .orderByAsc(FinancialProduct::getType) // 活期优先
                .orderByDesc(FinancialProduct::getRate); // 年化利率降序;
        return getFinancialProductVOIPage(new Page<>(1, Integer.MAX_VALUE), productType, query).getRecords();
    }

    private IPage<FinancialProductVO> getFinancialProductVOIPage(Page<FinancialProduct> page, ProductType type, LambdaQueryWrapper<FinancialProduct> query) {
        if (Objects.nonNull(type)) {
            query = query.eq(FinancialProduct::getType, type);
        }

        var list = financialProductService.page(page, query);
        List<Long> productIds = list.getRecords().stream().map(FinancialProduct::getId).distinct().collect(Collectors.toList());

        Boolean isNewUser = financialRecordService.isNewUser(requestInitService.uid());

        // 如果是新用户直接返回空map，减少无效查询
        Map<Long, Long> firstProcessRecordMap = isNewUser ? Collections.emptyMap() :
                financialRecordService.firstProcessRecordMap(productIds, requestInitService.uid());
        Map<Long, BigDecimal> useFinancialPersonQuotaMap = isNewUser ? Collections.emptyMap() :
                financialRecordService.getUseQuota(productIds, requestInitService.uid());

        return list.convert(product -> {
            BigDecimal totalQuota = product.getTotalQuota();
            BigDecimal personQuota = product.getPersonQuota();
            BigDecimal useQuota = MoreObjects.firstNonNull(product.getUseQuota(), BigDecimal.ZERO);
            var accountBalance = accountBalanceServiceImpl.getAndInit(requestInitService.uid(), product.getCoin());
            FinancialProductVO financialProductVO = financialConverter.toFinancialProductVO(product);

            // 设置额度信息
            financialProductVO.setAvailableBalance(accountBalance.getRemain());
            if (ProductType.fund.equals(product.getType())) {
                List<FundRecord> fundRecords = fundRecordService.listByUidAndProductId(requestInitService.uid(), product.getId());
                if (CollectionUtils.isNotEmpty(fundRecords)) {
                    BigDecimal holdAmount = fundRecords.stream().map(FundRecord::getHoldAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    financialProductVO.setUserPersonQuota(holdAmount);
                    financialProductVO.setHoldAmount(holdAmount);
                    financialProductVO.setRecordId(fundRecords.get(0).getId());
                }
            }

            if (!ProductType.fund.equals(product.getType())) {
                var usePersonQuota = useFinancialPersonQuotaMap.getOrDefault(product.getId(), BigDecimal.ZERO);
                financialProductVO.setUserPersonQuota(usePersonQuota);
                financialProductVO.setHoldAmount(usePersonQuota);
                // 设置第一个有效可数据的record id
                financialProductVO.setRecordId(firstProcessRecordMap.get(product.getId()));
                // 设置是否可以申购
                boolean allowPurchase =
                        checkAllowPurchase(usePersonQuota, useQuota, personQuota, totalQuota, product.getBusinessType(), isNewUser);
                financialProductVO.setAllowPurchase(allowPurchase);
            }

            // 设置是否持有
            financialProductVO.setHold(MoreObjects.firstNonNull(financialProductVO.getHoldAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0);

            // =================================== 下方的数据不依赖于用户信息================================================
            // 设置是否售罄
            if (Objects.nonNull(totalQuota)) {
                financialProductVO.setSellOut(useQuota.compareTo(totalQuota) >= 0);
            }
            // 设置假数据（基金不设置）
            BigDecimal baseDataAmount = getBaseDataAmount(product.getId(), totalQuota, useQuota);
            if (Objects.nonNull(baseDataAmount) & !ProductType.fund.equals(product.getType())) {
                financialProductVO.setUseQuota(useQuota.add(baseDataAmount));
                financialProductVO.setBaseUseQuota(baseDataAmount);
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startIncomeTime = now.plusDays(1);
            // 开始记息时间
            financialProductVO.setStartIncomeTime(startIncomeTime);
            // 申购时间
            financialProductVO.setPurchaseTime(now);
            // 收益发放时间
            financialProductVO.setSettleTime(startIncomeTime.plusDays(product.getTerm().getDay()));

            financialProductVO.setNewUser(isNewUser);
            financialProductVO.setCoins(coinService.pushCoinsWithCache(product.getCoin()).stream()
                    .map(chainConverter::toCoinVO).collect(Collectors.toList()));

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
        long randomNum = (Integer.parseInt(monthAndDay) + dayOfMonth * 100 + month * 100 + productId) % 65;
        randomNum = randomNum + 300;

        // 获取一个每天固定的随机比例 365 以内
        BigDecimal randomRate = BigDecimal.valueOf(randomNum).divide(BigDecimal.valueOf(365L), 4, RoundingMode.HALF_DOWN);

        // 调整比例 = 差值比例 - 差值比例 * 0.2 * 每天固定随机比例
        BigDecimal adjustRate = expectRate.multiply(BigDecimal.valueOf(0.6f).multiply(randomRate)).add(baseRate);

        return limitQuota.multiply(adjustRate);
    }


    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
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
    private FinancialBoardProductService financialBoardProductService;
    @Resource
    private FinancialBoardWalletService financialBoardWalletService;
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private RedisService redisService;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private CoinService coinService;
    @Resource
    private ChainConverter chainConverter;

}
