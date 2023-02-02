package com.tianli.product.service;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.product.dto.PurchaseResultDto;
import com.tianli.product.dto.RedeemResultDto;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.enums.ProductType;
import com.tianli.product.financial.mapper.FinancialProductMapper;
import com.tianli.product.financial.query.PurchaseQuery;
import com.tianli.product.financial.service.AbstractProductOperation;
import com.tianli.product.financial.vo.ExpectIncomeVO;
import com.tianli.product.fund.bo.FundPurchaseBO;
import com.tianli.product.fund.contant.FundTransactionStatus;
import com.tianli.product.fund.convert.FundRecordConvert;
import com.tianli.product.fund.entity.FundRecord;
import com.tianli.product.fund.entity.FundTransactionRecord;
import com.tianli.product.fund.enums.FundRecordStatus;
import com.tianli.product.fund.enums.FundTransactionType;
import com.tianli.product.fund.query.FundRecordQuery;
import com.tianli.product.fund.service.IFundRecordService;
import com.tianli.product.fund.service.IFundTransactionRecordService;
import com.tianli.product.fund.vo.FundTransactionRecordVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-01
 **/
@Service
public class FundProductService extends AbstractProductOperation<FinancialProductMapper, FinancialProduct> {

    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;
    @Resource
    private FundRecordConvert fundRecordConvert;
    @Resource
    private WebHookService webHookService;

    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        List<AmountDto> holdAmounts = fundRecordService.hold(FundRecordQuery.builder()
                .uid(uid).productId(product.getId()).build());
        var holdAmount = Opt.ofNullable(holdAmounts.get(0)).orElse(new AmountDto());
        if (product.getPersonQuota() != null && product.getPersonQuota().compareTo(BigDecimal.ZERO) > 0 &&
                amount.add(holdAmount.getAmount()).compareTo(product.getPersonQuota()) > 0) {
            ErrorCodeEnum.PURCHASE_GT_PERSON_QUOTA.throwException();
        }
    }

    @Override
    public PurchaseResultDto purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order) {
        Long productId = purchaseQuery.getProductId();
        var financialProduct = this.baseMapper.selectById(productId);
        WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(productId);
        boolean advance = Objects.nonNull(order) && order.getOrderNo().startsWith(AccountChangeType.advance_purchase.getPrefix());

        BigDecimal purchaseAmount = purchaseQuery.getAmount();
        FundRecord fundRecord;

        // 持有记录
        if (advance) {
            fundRecord = fundRecordService.getById(order.getRelatedId());
            fundRecord.setStatus(FundRecordStatus.PROCESS);
            fundRecordService.updateById(fundRecord);
        } else {
            fundRecord = FundRecord.builder()
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
            fundRecordService.save(fundRecord);

        }


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
        FundTransactionRecord transactionRecord;
        if (advance) {
            transactionRecord = fundTransactionRecordService.getById(order.getRelatedId());
            transactionRecord.setStatus(FundTransactionStatus.success);
            fundTransactionRecordService.updateById(transactionRecord);
        } else {
            transactionRecord = FundTransactionRecord.builder()
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
        }

        return PurchaseResultDto.builder()
                .recordId(transactionRecord.getFundId())
                .fundTransactionRecordVO(fundRecordConvert.toFundTransactionVO(transactionRecord))
                .build();
    }

    @Override
    public RedeemResultDto redeemOperation(Long uid, RedeemQuery redeemQuery) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpectIncomeVO exceptDailyIncome(Long uid, Long productId, Long recordId) {
        FundRecord fundRecord = fundRecordService.getById(recordId);
        BigDecimal holdAmount = fundRecord.getHoldAmount();
        BigDecimal rate = fundRecord.getRate();
        return new ExpectIncomeVO(holdAmount.multiply(rate).divide(new BigDecimal(365), 8, RoundingMode.DOWN));
    }

    @Override
    public BigDecimal incomeRate(Long uid, Long productId, Long recordId) {
        // 获取赎回金额
        FundRecord fundRecord = fundRecordService.getById(recordId);
        List<FundTransactionRecord> redemptionRecords = fundTransactionRecordService.list(new LambdaQueryWrapper<FundTransactionRecord>()
                .eq(FundTransactionRecord::getUid, uid)
                .eq(FundTransactionRecord::getFundId, recordId)
                .eq(FundTransactionRecord::getStatus, FundTransactionStatus.success)
                .eq(FundTransactionRecord::getProductId, productId)
                .eq(FundTransactionRecord::getType, FundTransactionType.redemption));

        BigDecimal calIncomeAmount = fundRecord.getCumulativeIncomeAmount();

        BigDecimal allHoldAmount = fundRecord.getHoldAmount()
                .add(redemptionRecords.stream().map(FundTransactionRecord::getTransactionAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return calIncomeAmount.divide(allHoldAmount, 4, RoundingMode.HALF_UP);
    }

    @Override
    public void finishPurchase(Long uid, FinancialProduct product, PurchaseQuery purchaseQuery) {
        String msg = WebHookTemplate.fundPurchase(uid, product.getName(), purchaseQuery.getAmount(), product.getCoin());
        webHookService.fundSend(msg);
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
