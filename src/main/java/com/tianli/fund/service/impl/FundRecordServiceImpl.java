package com.tianli.fund.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.AbstractProductOperation;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.fund.bo.FundPurchaseBO;
import com.tianli.fund.bo.FundRedemptionBO;
import com.tianli.fund.contant.FundCycle;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.contant.FundTransactionStatus;
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundRecordMapper;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundReview;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundRecordStatus;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundReviewService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.fund.vo.*;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.FundUserHoldDto;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.vo.FundUserRecordVO;
import com.tianli.management.vo.HoldUserAmount;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class FundRecordServiceImpl extends AbstractProductOperation<FundRecordMapper, FundRecord> implements IFundRecordService {

    @Resource
    private FundRecordMapper fundRecordMapper;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private FundRecordConvert fundRecordConvert;

    @Resource
    private OrderService orderService;

    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;

    @Resource
    private FinancialProductService financialProductService;

    @Resource
    private FinancialRecordService financialRecordService;

    @Resource
    private AccountBalanceService accountBalanceService;

    @Resource
    private IWalletAgentProductService walletAgentProductService;

    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;

    @Resource
    private IFundReviewService fundReviewService;

    @Override
    @SuppressWarnings("unchecked")
    public FundTransactionRecordVO purchaseOperation(Long uid, PurchaseQuery purchaseQuery) {
        return purchaseOperation(uid, purchaseQuery, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FundTransactionRecordVO purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order) {
        Long productId = purchaseQuery.getProductId();
        var financialProduct = financialProductService.getById(productId);
        WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(productId);

        BigDecimal purchaseAmount = purchaseQuery.getAmount();
        //持有记录
        FundRecord fundRecord = FundRecord.builder()
                .uid(uid)
                .productId(productId)
                .productName(financialProduct.getName())
                .productNameEn(financialProduct.getNameEn())
                .coin(financialProduct.getCoin())
                .logo(financialProduct.getLogo())
                .holdAmount(purchaseAmount)
                .riskType(financialProduct.getRiskType())
                .businessType(financialProduct.getBusinessType())
                .rate(financialProduct.getRate())
                .status(FundRecordStatus.PROCESS)
                .createTime(LocalDateTime.now())
                .type(ProductType.fund)
                .build();
        this.save(fundRecord);

        if (Objects.isNull(order)) {
            //生成一笔订单
            order = Order.builder()
                    .uid(uid)
                    .coin(financialProduct.getCoin())
                    .relatedId(fundRecord.getId())
                    .orderNo(AccountChangeType.fund_purchase.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                    .amount(purchaseAmount)
                    .type(ChargeType.fund_purchase)
                    .status(ChargeStatus.chain_success)
                    .createTime(LocalDateTime.now())
                    .completeTime(LocalDateTime.now())
                    .build();
            orderService.save(order);
        }
        // 减少余额
        accountBalanceService.decrease(uid, ChargeType.fund_purchase, financialProduct.getCoin(), purchaseAmount, order.getOrderNo(), CurrencyLogDes.基金申购.name());
        //代理人钱包
        AccountBalance agentAccountBalance = accountBalanceService.getAndInit(walletAgentProduct.getUid(), financialProduct.getCoin());
        //代理人生成一笔订单
        Order agentOrder = Order.builder()
                .uid(agentAccountBalance.getUid())
                .coin(financialProduct.getCoin())
                .relatedId(fundRecord.getId())
                .orderNo(AccountChangeType.agent_fund_sale.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(purchaseAmount)
                .type(ChargeType.agent_fund_sale)
                .status(ChargeStatus.chain_success)
                .createTime(LocalDateTime.now())
                .completeTime(LocalDateTime.now())
                .build();
        orderService.save(agentOrder);
        accountBalanceService.increase(agentAccountBalance.getUid(), ChargeType.agent_fund_sale, financialProduct.getCoin(), purchaseAmount, order.getOrderNo(), CurrencyLogDes.代理基金销售.name());
        //交易记录
        FundTransactionRecord transactionRecord = FundTransactionRecord.builder()
                .uid(uid)
                .fundId(fundRecord.getId())
                .productId(fundRecord.getProductId())
                .productName(fundRecord.getProductName())
                .coin(fundRecord.getCoin())
                .rate(fundRecord.getRate())
                .type(FundTransactionType.purchase)
                .status(FundTransactionStatus.success)
                .transactionAmount(purchaseAmount)
                .createTime(LocalDateTime.now()).build();
        fundTransactionRecordService.save(transactionRecord);

        return fundRecordConvert.toFundTransactionVO(transactionRecord);
    }

    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct financialProduct, BigDecimal amount) {

        BigDecimal personHoldAmount = fundRecordMapper.selectHoldAmountSum(financialProduct.getId(), uid);
        if (financialProduct.getPersonQuota() != null && financialProduct.getPersonQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(personHoldAmount).compareTo(financialProduct.getPersonQuota()) > 0) {
            ErrorCodeEnum.PURCHASE_GT_PERSON_QUOTA.throwException();
        }

    }

    @Override
    public FundMainPageVO mainPage() {
        Long uid = requestInitService.uid();
        List<AmountDto> holdAmount = fundRecordMapper.holdAmountSumByUid(uid, null);
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(query);
        List<AmountDto> waitPayInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncome -> new AmountDto(fundIncome.getWaitInterestAmount(), fundIncome.getCoin())).collect(Collectors.toList());
        List<AmountDto> payInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncome -> new AmountDto(fundIncome.getPayInterestAmount(), fundIncome.getCoin())).collect(Collectors.toList());
        return FundMainPageVO.builder()
                .holdAmount(orderService.calDollarAmount(holdAmount))
                .payInterestAmount(orderService.calDollarAmount(payInterestAmount))
                .waitPayInterestAmount(orderService.calDollarAmount(waitPayInterestAmount))
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
        BigDecimal totalAmount = fundRecordMapper.selectHoldAmountSum(productId, null);
        BigDecimal personHoldAmount = fundRecordMapper.selectHoldAmountSum(productId, uid);
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, product.getCoin());
        return FundApplyPageVO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productNameEn(product.getNameEn())
                .coin(product.getCoin())
                .logo(product.getLogo())
                .rate(product.getRate())
                .availableAmount(accountBalance.getRemain())
                .expectedIncome(dailyIncome(purchaseAmount, product.getRate()))
                .personQuota(product.getPersonQuota())
                .personHoldAmount(personHoldAmount)
                .totalQuota(product.getTotalQuota())
                .totalHoldAmount(totalAmount)
                .purchaseTime(LocalDate.now())
                .interestCalculationTime(LocalDate.now().plusDays(FundCycle.interestCalculationCycle))
                .incomeDistributionTime(LocalDate.now().plusDays(FundCycle.incomeDistributionCycle))
                .redemptionTime(LocalDate.now().plusDays(FundCycle.incomeDistributionCycle))
                .redemptionCycle(FundCycle.interestAuditCycle)
                .accountDate(FundCycle.accountCycle)
                .build();
    }

    @Override
    public FundTransactionRecordVO purchase(FundPurchaseBO bo) {
        return this.purchase(requestInitService.uid(), bo, FundTransactionRecordVO.class);
    }

    @Override
    public FundRecordVO detail(Long id) {
        FundRecord fundRecord = this.getById(id);
        FundRecordVO fundRecordVO = fundRecordConvert.toFundVO(fundRecord);
        long until = fundRecord.getCreateTime().until(LocalDateTime.now(), ChronoUnit.DAYS);
        //七天允许赎回
        // todo 第二天起计算七天
        fundRecordVO.setIsAllowRedemption(until >= FundCycle.interestAuditCycle);
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
            fundTransactionRecordVO.setExpectedIncome(dailyIncome(fundTransactionRecordVO.getTransactionAmount(), fundTransactionRecordVO.getRate()));
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
            List<AmountDto> amountDtos = fundRecordMapper.holdAmountSumByUid(uid, query.getAgentId());
            fundUserRecordVO.setHoldAmount(orderService.calDollarAmount(amountDtos));
            // 累计利息 包括 待发 + 已经发
            List<AmountDto> interestAmount = fundIncomeRecordService.getAmountByUidAndStatus(uid, query.getAgentId(), new ArrayList<>());
            fundUserRecordVO.setInterestAmount(orderService.calDollarAmount(interestAmount));

            // 已经支付利息
            List<AmountDto> payInterestAmount = fundIncomeRecordService.getAmountByUidAndStatus(uid, query.getAgentId(), FundIncomeStatus.audit_success);
            fundUserRecordVO.setPayInterestAmount(orderService.calDollarAmount(payInterestAmount));

            // 待支付利息
            BigDecimal waitPayInterestAmount = orderService.calDollarAmount(fundIncomeRecordService.getAmountByUidAndStatus(uid, query.getAgentId()
                    , List.of(FundIncomeStatus.wait_audit, FundIncomeStatus.calculated)));
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
                .holdAmount(orderService.calDollarAmount(holdAmountDtos))
                .interestAmount(orderService.calDollarAmount(interestAmountDtos))
                .waitInterestAmount(orderService.calDollarAmount(waitInterestAmountDtos))
                .payInterestAmount(orderService.calDollarAmount(payInterestDtos))
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

        // 记息时间 T 为 申购时间第二天零
        LocalDate startIncomeTime = fundRecord.getCreateTime().toLocalDate().plusDays(1);
        if (startIncomeTime.until(LocalDateTime.now(), ChronoUnit.DAYS) < FundCycle.interestAuditCycle) {
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

        return FundTransactionRecordVO.builder()
                .id(transactionRecord.getId())
                .productName(transactionRecord.getProductName())
                .coin(transactionRecord.getCoin())
                .transactionAmount(redemptionAmount)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public BigDecimal dailyIncome(BigDecimal holdAmount, BigDecimal rate) {
        return holdAmount.multiply(rate).divide(new BigDecimal(365), 8, RoundingMode.DOWN);
    }

    @Override
    public BigDecimal getHoldAmount(FundRecordQuery query) {
        List<AmountDto> amountDtos = fundRecordMapper.selectHoldAmount(query);
        return orderService.calDollarAmount(amountDtos);
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
    public void validProduct(FinancialProduct financialProduct, PurchaseQuery purchaseQuery) {

        FundPurchaseBO bo = (FundPurchaseBO) purchaseQuery;
        String referralCode = bo.getReferralCode();
        WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(financialProduct.getId());

        if (Objects.isNull(walletAgentProduct)) ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        if (!walletAgentProduct.getReferralCode().equals(referralCode))
            ErrorCodeEnum.REFERRAL_CODE_ERROR.throwException();

    }
}