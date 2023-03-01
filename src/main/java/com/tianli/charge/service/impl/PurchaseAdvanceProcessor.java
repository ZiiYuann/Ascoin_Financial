package com.tianli.charge.service.impl;

import com.tianli.account.enums.AccountChangeType;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.charge.service.OrderService;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.charge.vo.OrderFundTransactionVO;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RecordStatus;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.contant.FundTransactionStatus;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.enums.FundRecordStatus;
import com.tianli.product.afund.enums.FundTransactionType;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.service.FinancialProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class PurchaseAdvanceProcessor implements OrderAdvanceProcessor<Void> {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private OrderService orderService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;

    @Override
    public AdvanceType getType() {
        return AdvanceType.PURCHASE;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {

        FinancialProduct product = financialProductService.getById(query.getProductId());
        if (!ProductType.fund.equals(product.getType())) {
            return;
        }

        WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(product.getId());
        if (!walletAgentProduct.getReferralCode().equals(query.getReferralCode())) {
            ErrorCodeEnum.REFERRAL_CODE_ERROR.throwException();
        }

    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setProductId(query.getProductId());
        orderAdvance.setCoin(query.getCoin());
        orderAdvance.setTerm(query.getTerm());
        orderAdvance.setTerm(query.getTerm());
    }

    @Override
    public OrderBaseVO update(OrderAdvance orderAdvance) {

        Long uid = orderAdvance.getUid();
        Long productId = orderAdvance.getProductId();

        FinancialProduct product = financialProductService.getById(productId);

        // 生成一笔预订单记录
        Order order = Order.builder()
                .uid(uid)
                .coin(product.getCoin())
                .orderNo(AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId())
                .amount(orderAdvance.getAmount())
                .type(ProductType.fund.equals(product.getType()) ? ChargeType.fund_purchase : ChargeType.purchase)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .status(ChargeStatus.chaining)
                .relatedId(orderAdvance.getId())
                .build();
        orderService.save(order);

        if (ProductType.fixed.equals(product.getType()) || ProductType.current.equals(product.getType())) {
            // 预订单和持有record 订单id一致
            FinancialRecord financialRecord =
                    financialRecordService.generateFinancialRecord(orderAdvance.getId(), uid, product, orderAdvance.getAmount()
                            , orderAdvance.isAutoCurrent());

            // 预订单
            financialRecord.setStatus(RecordStatus.SUCCESS);
            financialRecord.setLocalPurchase(true);
            financialRecordService.updateById(financialRecord);
            return chargeService.orderDetails(uid
                    , AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId());

        }


        if (ProductType.fund.equals(product.getType())) {

            FundRecord fundRecord = FundRecord.builder()
                    .id(orderAdvance.getId())
                    .uid(uid)
                    .productId(productId)
                    .productName(product.getName())
                    .productNameEn(product.getNameEn())
                    .coin(product.getCoin())
                    .logo(product.getLogo())
                    .holdAmount(orderAdvance.getAmount())
                    .riskType(product.getRiskType())
                    .businessType(product.getBusinessType())
                    .rate(product.getRate())
                    .status(FundRecordStatus.SUCCESS)
                    .createTime(LocalDateTime.now())
                    .type(ProductType.fund)
                    .build();
            fundRecordService.getBaseMapper().insert(fundRecord);

            //交易记录
            FundTransactionRecord transactionRecord = FundTransactionRecord.builder()
                    .id(orderAdvance.getId())
                    .uid(uid)
                    .fundId(fundRecord.getId())
                    .productId(fundRecord.getProductId())
                    .productName(fundRecord.getProductName())
                    .coin(fundRecord.getCoin())
                    .rate(fundRecord.getRate())
                    .type(FundTransactionType.purchase)
                    .status(FundTransactionStatus.processing)
                    .transactionAmount(orderAdvance.getAmount())
                    .createTime(order.getCreateTime()).build();
            fundTransactionRecordService.getBaseMapper().insert(transactionRecord);

            OrderFundTransactionVO orderFundTransactionVO = OrderFundTransactionVO.builder()
                    .status(2)
                    .createTime(order.getCreateTime())
                    .rate(product.getRate().doubleValue())
                    .coin(product.getCoin())
                    .id(orderAdvance.getId())
                    .transactionAmount(orderAdvance.getAmount())
                    .expectedIncome(
                            financialProductService.exceptDailyIncome(orderAdvance.getAmount(), product.getRate(), 365)
                                    .getExpectIncome()
                    )
                    .build();
            orderFundTransactionVO.setProductName(product.getName());
            orderFundTransactionVO.setProductNameEn(product.getNameEn());
            orderFundTransactionVO.setType(FundTransactionType.purchase);
            return orderFundTransactionVO;
        }

        return null;
    }

    @Override
    public Void getQuery(OrderAdvance orderAdvance) {
        return null;
    }
}
