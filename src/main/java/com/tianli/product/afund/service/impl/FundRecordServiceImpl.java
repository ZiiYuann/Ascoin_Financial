package com.tianli.product.afund.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.common.PageQuery;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.FundUserHoldDto;
import com.tianli.management.vo.FundUserRecordVO;
import com.tianli.management.vo.HoldUserAmount;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductStatus;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afund.bo.FundRedemptionBO;
import com.tianli.product.afund.contant.FundCycle;
import com.tianli.product.afund.contant.FundIncomeStatus;
import com.tianli.product.afund.contant.FundTransactionStatus;
import com.tianli.product.afund.convert.FundRecordConvert;
import com.tianli.product.afund.dao.FundRecordMapper;
import com.tianli.product.afund.dto.FundIncomeAmountDTO;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundReview;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.enums.FundRecordStatus;
import com.tianli.product.afund.enums.FundTransactionType;
import com.tianli.product.afund.query.FundIncomeQuery;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.service.IFundReviewService;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.afund.vo.*;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.service.FundProductService;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 基金持有记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundRecordServiceImpl extends ServiceImpl<FundRecordMapper, FundRecord> implements IFundRecordService {

    @Resource
    private FundRecordMapper fundRecordMapper;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FundRecordConvert fundRecordConvert;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;
    @Resource
    private IFundReviewService fundReviewService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private FundProductService fundProductService;

    @Override
    public FundMainPageVO mainPage(Long uid) {
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(query);
        List<AmountDto> waitPayInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncome -> new AmountDto(fundIncome.getWaitInterestAmount(), fundIncome.getCoin())).collect(Collectors.toList());
        List<AmountDto> payInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncome -> new AmountDto(fundIncome.getPayInterestAmount(), fundIncome.getCoin())).collect(Collectors.toList());
        return FundMainPageVO.builder()
                .holdAmount(this.dollarHold(FundRecordQuery.builder().uid(uid).build()))
                .payInterestAmount(currencyService.calDollarAmount(payInterestAmount))
                .waitPayInterestAmount(currencyService.calDollarAmount(waitPayInterestAmount))
                .build();
    }

    @Override
    public IPage<FundProductVO> productPage(PageQuery<FinancialProduct> page) {
        return financialProductService.page(page.page(),
                        new QueryWrapper<FinancialProduct>().lambda().eq(FinancialProduct::getType, ProductType.fund)
                                .eq(FinancialProduct::getStatus, ProductStatus.open)
                                .eq(FinancialProduct::isDeleted, 0)
                                .orderByDesc(FinancialProduct::getRate))
                .convert(fundRecordConvert::toProductVO);
    }

    @Override
    public IPage<FundRecordVO> fundRecordPage(PageQuery<FundRecord> page) {
        Long uid = requestInitService.uid();
        return this.page(page.page(), new QueryWrapper<FundRecord>().lambda()
                        .eq(FundRecord::getUid, uid)
                        .eq(FundRecord::getStatus, FundRecordStatus.PROCESS))
                .convert(fundRecordConvert::toFundVO);
    }

    @Override
    public FundApplyPageVO applyPage(Long productId, BigDecimal purchaseAmount) {

        FinancialProduct product = financialProductService.getById(productId);
        if (Objects.isNull(product) || !product.getType().equals(ProductType.fund))
            ErrorCodeEnum.AGENT_PRODUCT_NOT_EXIST.throwException();
        Long uid = requestInitService.uid();
        BigDecimal personHoldAmount = fundRecordMapper.selectHoldAmountSum(productId, uid);
        AccountBalance accountBalance = accountBalanceServiceImpl.getAndInit(uid, product.getCoin());
        return FundApplyPageVO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productNameEn(product.getNameEn())
                .coin(product.getCoin())
                .logo(product.getLogo())
                .rate(product.getRate())
                .availableAmount(accountBalance.getRemain())
                .expectedIncome(fundProductService.exceptDailyIncome(purchaseAmount, product.getRate(), 365).getExpectIncome())
                .personQuota(product.getPersonQuota())
                .personHoldAmount(personHoldAmount)
                .totalQuota(product.getTotalQuota())
                .totalHoldAmount(product.getUseQuota())
                .purchaseTime(LocalDate.now())
                .interestCalculationTime(LocalDate.now().plusDays(FundCycle.interestCalculationCycle))
                .incomeDistributionTime(LocalDate.now().plusDays(FundCycle.incomeDistributionCycle))
                .redemptionTime(LocalDate.now().plusDays(FundCycle.incomeDistributionCycle))
                .redemptionCycle(FundCycle.interestAuditCycle)
                .accountDate(FundCycle.accountCycle)
                .build();
    }

    @Override
    public FundRecordVO detail(Long uid, Long id) {
        FundRecord fundRecord = this.getById(id);
        FundRecordVO fundRecordVO = fundRecordConvert.toFundVO(fundRecord);
        long until = fundRecord.getCreateTime().until(LocalDateTime.now(), ChronoUnit.DAYS);
        fundRecordVO.setIsAllowRedemption(until >= FundCycle.interestAuditCycle);

        FinancialProduct product = financialProductService.getById(fundRecord.getProductId());

        if (Objects.nonNull(product.getTotalQuota())) {
            BigDecimal useQuota = MoreObjects.firstNonNull(product.getUseQuota(), BigDecimal.ZERO);
            fundRecordVO.setSellOut(useQuota.compareTo(product.getTotalQuota()) >= 0);
        }

        // 设置基金昨日收益
        fundRecordVO.setYesterdayIncomeAmount(fundIncomeRecordService.yesterdayIncomeAmount(id));
        fundRecordVO.setEarningRate(fundProductService.incomeRate(uid, fundRecord.getProductId(), id));
        return fundRecordVO;
    }

    @Override
    public IPage<FundIncomeRecordVO> incomeSummary(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        Long uid = requestInitService.uid();
        query.setUid(uid);
        return fundIncomeRecordService.getSummaryPage(page, query);
    }

    @Override
    public IPage<FundIncomeRecordVO> incomeRecord(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        Long uid = requestInitService.uid();
        query.setUid(uid);
        return fundIncomeRecordService.getPage(page, query);
    }

    @Override
    public FundRecordVO redemptionPage(Long id) {
        FundRecord fundRecord = this.getById(id);
        if (Objects.isNull(fundRecord)) ErrorCodeEnum.FUND_NOT_EXIST.throwException();
        return FundRecordVO.builder()
                .id(fundRecord.getId())
                .productName((fundRecord.getProductName()))
                .coin(fundRecord.getCoin())
                .logo(fundRecord.getLogo())
                .rate(fundRecord.getRate())
                .holdAmount(fundRecord.getHoldAmount())
                .build();
    }

    @Override
    public IPage<FundTransactionRecordVO> transactionRecord(PageQuery<FundTransactionRecord> page, FundTransactionQuery query) {
        return fundTransactionRecordService.getTransactionPage(page, query);
    }

    @Override
    public FundTransactionRecordVO transactionDetail(Long transactionId) {
        FundTransactionRecord transactionRecord = fundTransactionRecordService.getById(transactionId);
        FundTransactionRecordVO fundTransactionRecordVO = fundRecordConvert.toFundTransactionVO(transactionRecord);
        FundRecord fundRecord = this.getById(transactionRecord.getFundId());
        if (fundTransactionRecordVO.getType() == FundTransactionType.purchase) {
            fundTransactionRecordVO.setExpectedIncome(fundProductService.exceptDailyIncome
                            (fundTransactionRecordVO.getTransactionAmount(), fundTransactionRecordVO.getRate(), 365)
                    .getExpectIncome()
            );
        }
        if (fundTransactionRecordVO.getType() == FundTransactionType.redemption && Objects.equals(fundTransactionRecordVO.getStatus(), FundTransactionStatus.success)) {
            List<FundReview> reviews = fundReviewService.getListByRid(transactionRecord.getId());
            FundReview fundReview = CollUtil.getFirst(reviews);
            if (Objects.nonNull(fundReview)) {
                fundTransactionRecordVO.setAccountTate(fundReview.getCreateTime());
            }
        }
        fundTransactionRecordVO.setProductNameEn(fundRecord.getProductNameEn());
        return fundTransactionRecordVO;
    }

    @Override
    public IPage<FundUserRecordVO> fundUserRecordPage(PageQuery<FundRecord> pageQuery, FundRecordQuery query) {
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordMapper.selectDistinctUidPage(pageQuery.page(), query);
        return fundUserRecordPage.convert(fundUserRecordVO -> {
            Long uid = fundUserRecordVO.getUid();
            fundUserRecordVO.setHoldAmount(this.dollarHold(
                    FundRecordQuery.builder()
                            .uid(uid)
                            .agentId(query.getAgentId())
                            .build()));
            // 累计利息 包括 待发 + 已经发
            fundUserRecordVO.setInterestAmount(fundIncomeRecordService.amountDollar(uid, query.getAgentId(), new ArrayList<>()));

            // 已经支付利息
            fundUserRecordVO.setPayInterestAmount(fundIncomeRecordService.amountDollar(uid, query.getAgentId(), FundIncomeStatus.audit_success));

            // 待支付利息
            BigDecimal waitPayInterestAmount = fundIncomeRecordService.amountDollar(uid, query.getAgentId()
                    , List.of(FundIncomeStatus.wait_audit, FundIncomeStatus.calculated));
            fundUserRecordVO.setWaitPayInterestAmount(waitPayInterestAmount);
            return fundUserRecordVO;
        });
    }

    @Override
    public HoldUserAmount fundUserAmount(FundRecordQuery query) {
        List<FundUserHoldDto> fundUserHoldDtos = fundRecordMapper.selectFundUserHoldDto(query);

        // 持有金额
        List<AmountDto> holdAmountDtos = fundUserHoldDtos.stream().map(fundUserHoldDto ->
                new AmountDto(fundUserHoldDto.getHoldAmount(), fundUserHoldDto.getCoin())).collect(Collectors.toList());
        // 累计收益
        List<AmountDto> interestAmountDtos = fundUserHoldDtos.stream().map(fundUserHoldDto ->
                new AmountDto(fundUserHoldDto.getInterestAmount(), fundUserHoldDto.getCoin())).collect(Collectors.toList());
        // 待发放收益
        List<AmountDto> waitInterestAmountDtos = fundUserHoldDtos.stream().map(fundUserHoldDto ->
                new AmountDto(fundUserHoldDto.getWaitInterestAmount(), fundUserHoldDto.getCoin())).collect(Collectors.toList());
        // 已经发放收益
        List<AmountDto> payInterestDtos = fundUserHoldDtos.stream().map(fundUserHoldDto ->
                new AmountDto(fundUserHoldDto.getPayInterestAmount(), fundUserHoldDto.getCoin())).collect(Collectors.toList());
        return HoldUserAmount.builder()
                .holdAmount(currencyService.calDollarAmount(holdAmountDtos))
                .interestAmount(currencyService.calDollarAmount(interestAmountDtos))
                .waitInterestAmount(currencyService.calDollarAmount(waitInterestAmountDtos))
                .payInterestAmount(currencyService.calDollarAmount(payInterestDtos))
                .build();
    }

    @Override
    public FundTransactionRecordVO applyRedemption(FundRedemptionBO bo) {
        Long id = bo.getId();
        BigDecimal redemptionAmount = bo.getRedemptionAmount();
        FundRecord fundRecord = this.getById(id);
        if (Objects.isNull(fundRecord)) ErrorCodeEnum.FUND_NOT_EXIST.throwException();
        if (redemptionAmount.compareTo(fundRecord.getHoldAmount()) > 0)
            ErrorCodeEnum.REDEMPTION_GT_HOLD.throwException();
        if (redemptionAmount.compareTo(fundRecord.getHoldAmount()) == 0) {
            fundRecord.setStatus(FundRecordStatus.SUCCESS);
            this.updateById(fundRecord);
        }

        if (fundRecord.getCreateTime().until(LocalDateTime.now(), ChronoUnit.DAYS) < FundCycle.interestAuditCycle) {
            ErrorCodeEnum.REDEMPTION_CYCLE_ERROR.throwException();
        }


        //扣除余额
        fundRecordMapper.reduceAmount(id, redemptionAmount);

        //添加交易记录
        FundTransactionRecord transactionRecord = FundTransactionRecord.builder()
                .uid(fundRecord.getUid())
                .fundId(fundRecord.getId())
                .productId(fundRecord.getProductId())
                .productName(fundRecord.getProductName())
                .coin(fundRecord.getCoin())
                .rate(fundRecord.getRate())
                .type(FundTransactionType.redemption)
                .status(FundTransactionStatus.wait_audit)
                .transactionAmount(redemptionAmount)
                .createTime(LocalDateTime.now()).build();
        fundTransactionRecordService.save(transactionRecord);

        String msg =
                WebHookTemplate.fundRedeem(fundRecord.getUid(), fundRecord.getProductName(), redemptionAmount, fundRecord.getCoin());
        webHookService.fundSend(msg);

        return FundTransactionRecordVO.builder()
                .id(transactionRecord.getId())
                .productName(transactionRecord.getProductName())
                .coin(transactionRecord.getCoin())
                .transactionAmount(redemptionAmount)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public BigDecimal holdSingleCoin(Long uid, String coin, Long agentId) {
        if (Objects.isNull(coin)) {
            throw new UnsupportedOperationException();
        }
        FundRecordQuery query = new FundRecordQuery();
        query.setUid(uid);
        query.setCoin(query.getCoin());
        query.setAgentId(agentId);
        query.setCoin(coin);
        List<AmountDto> amountDtos = fundRecordMapper.selectHoldAmount(query);
        return amountDtos.stream().map(AmountDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public BigDecimal dollarHold(FundRecordQuery query) {
        List<AmountDto> amountDtos = fundRecordMapper.selectHoldAmount(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    @Override
    public List<AmountDto> hold(FundRecordQuery query) {
        return fundRecordMapper.selectHoldAmount(query);
    }

    @Override
    public Integer getHoldUserCount(FundRecordQuery query) {
        return fundRecordMapper.selectHoldUserCount(query);
    }

    @Override
    public void increaseAmount(Long id, BigDecimal amount) {
        fundRecordMapper.increaseAmount(id, amount);
    }

    @Override
    public void updateRateByProductId(Long id, BigDecimal rate) {
        int hour = LocalDateTime.now().getHour();
        if (hour <= 2) {
            ErrorCodeEnum.throwException("计算利息时间段不允许修改产品年华利率");
        }
        fundRecordMapper.updateRateByProductId(id, rate);
    }

    @Override
    public List<FundRecord> listByUidAndProductId(Long uid, Long productId) {
        LambdaQueryWrapper<FundRecord> eq = new LambdaQueryWrapper<FundRecord>()
                .eq(FundRecord::getUid, uid)
                .eq(FundRecord::getProductId, productId)
                .eq(FundRecord::getStatus, FundRecordStatus.PROCESS)
                .eq(false, FundRecord::getHoldAmount, BigDecimal.ZERO);

        return Optional.ofNullable(fundRecordMapper.selectList(eq)).orElse(new ArrayList<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, BigDecimal> accrueIncomeAmount(List<Long> uids) {
        Map<Long, BigDecimal> result = new HashMap<>();
        List<FundRecord> allFundRecords = list(new LambdaQueryWrapper<FundRecord>()
                .in(FundRecord::getUid, uids));
        Map<Long, List<FundRecord>> fundRecordMap = allFundRecords.stream().collect(Collectors.groupingBy(FundRecord::getUid));

        for (Long uid : uids) {
            List<FundRecord> fundRecords = fundRecordMap.getOrDefault(uid, (List<FundRecord>) CollectionUtils.EMPTY_COLLECTION);
            List<AmountDto> amountDtos = fundRecords.stream().map(record -> {
                AmountDto amountDto = new AmountDto();
                amountDto.setAmount(record.getCumulativeIncomeAmount());
                amountDto.setCoin(record.getCoin());
                return amountDto;
            }).collect(Collectors.toList());
            BigDecimal amount = currencyService.calDollarAmount(amountDtos);
            result.put(uid, amount);
        }
        return result;
    }

}
