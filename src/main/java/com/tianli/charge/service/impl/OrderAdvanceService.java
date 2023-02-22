package com.tianli.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderService;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.charge.vo.OrderFundTransactionVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductStatus;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.enums.RecordStatus;
import com.tianli.product.afinancial.query.PurchaseQuery;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.bo.FundPurchaseBO;
import com.tianli.product.afund.contant.FundTransactionStatus;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.enums.FundRecordStatus;
import com.tianli.product.afund.enums.FundTransactionType;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.afund.service.impl.FundRecordServiceImpl;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.service.FundProductService;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Service
public class OrderAdvanceService extends ServiceImpl<OrderAdvanceMapper, OrderAdvance>
        implements com.tianli.charge.service.OrderAdvanceService {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private OrderService orderService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private FundRecordServiceImpl fundRecordService;
    @Resource
    private FundProductService fundProductService;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;


    /**
     * 生成预订单
     */
    @Override
    @Transactional
    @NoRepeatCommit(autoUnlock = false)
    public Long generateOrderAdvance(GenerateOrderAdvanceQuery query) {

        Long uid = requestInitService.uid();

        var builder = OrderAdvance.builder()
                .id(CommonFunction.generalId())
                .amount(query.getAmount())
                .uid(uid)
                .createTime(LocalDateTime.now())
                .advanceType(query.getAdvanceType());

        FinancialProduct product = financialProductService.getById(query.getProductId());
        if (AdvanceType.PURCHASE.equals(query.getAdvanceType()) && ProductType.fund.equals(product.getType())) {
            WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(product.getId());
            if (!walletAgentProduct.getReferralCode().equals(query.getReferralCode())) {
                ErrorCodeEnum.REFERRAL_CODE_ERROR.throwException();
            }
            builder = builder
                    .productId(query.getProductId())
                    .coin(product.getCoin())
                    .term(product.getTerm())
                    .autoCurrent(query.isAutoCurrent());
        }

        OrderAdvance orderAdvance = builder.build();
        baseMapper.insert(orderAdvance);

        return orderAdvance.getId();
    }

    /**
     * 更新预订单
     */
    @Transactional
    @Override
    public OrderBaseVO updateOrderAdvance(GenerateOrderAdvanceQuery query) {

        if (StringUtils.isBlank(query.getTxid())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        OrderAdvance orderAdvance = baseMapper.selectById(query.getId());
        orderAdvance.setTxid(query.getTxid());
        orderAdvance.setNetwork(query.getNetwork());
        baseMapper.updateById(orderAdvance);

        if (AdvanceType.PURCHASE.equals(orderAdvance.getAdvanceType())) {
            return updateByPurchase(orderAdvance);
        }
        if (AdvanceType.PLEDGE.equals(orderAdvance.getAdvanceType())
                || AdvanceType.REPAY.equals(orderAdvance.getAdvanceType())) {
            return updateByLoan(orderAdvance);
        }

        return null;
    }

    private OrderBaseVO updateByLoan(OrderAdvance orderAdvance) {
        AdvanceType advanceType = orderAdvance.getAdvanceType();
        AccountChangeType accountChangeType = null;
        ChargeType chargeType = null;
        if (AdvanceType.PLEDGE.equals(advanceType)) {
            accountChangeType = AccountChangeType.advance_borrow;
            chargeType = ChargeType.borrow;
        } else {
            accountChangeType = AccountChangeType.advance_repay;
            chargeType = ChargeType.repay;
        }


        Order order = Order.generate(orderAdvance.getId(),
                chargeType, orderAdvance.getCoin(), orderAdvance.getAmount(), orderAdvance.getId());
        order.setStatus(ChargeStatus.chaining);
        order.setOrderNo(accountChangeType + CommonFunction.generalSn(order.getId()));
        orderService.save(order);

        return chargeService.orderDetails(requestInitService.uid(), order.getOrderNo());
    }

    private OrderBaseVO updateByPurchase(OrderAdvance orderAdvance) {


        Long uid = orderAdvance.getUid();
        Long productId = orderAdvance.getProductId();

        FinancialProduct product = financialProductService.getById(productId);

        // 生成一笔预订单记录
        Order order = Order.builder()
                .uid(requestInitService.uid())
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

        if (!ProductType.fund.equals(product.getType())) {
            // 预订单和持有record 订单id一致
            FinancialRecord financialRecord =
                    financialRecordService.generateFinancialRecord(orderAdvance.getId(), uid, product, orderAdvance.getAmount()
                            , orderAdvance.isAutoCurrent());

            // 预订单
            financialRecord.setStatus(RecordStatus.SUCCESS);
            financialRecord.setLocalPurchase(true);
            financialRecordService.updateById(financialRecord);
            return chargeService.orderDetails(requestInitService.uid()
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


    /**
     * 处理充值事件
     */
    @Override
    @Transactional
    public void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, Coin coin) {

        try {
            Thread.sleep(5000);

            var query = new LambdaQueryWrapper<OrderAdvance>()
                    .eq(OrderAdvance::getUid, uid)
                    .eq(OrderAdvance::getTxid, req.getHash())
                    .eq(OrderAdvance::getFinish, 0);
            OrderAdvance orderAdvance = baseMapper.selectOne(query);
            if (Objects.isNull(orderAdvance)) {
                return;
            }

            // 增加尝试次数【不受本身事务的影响】
            addTryTimes(orderAdvance.getId());

            Long productId = orderAdvance.getProductId();
            FinancialProduct product = financialProductService.getById(productId);
            if (Objects.isNull(product)) {
                log.error("预订单产品不存在，请注意");
                return;
            }

            if (ProductStatus.close.equals(product.getStatus())) {
                log.error("预订单产品处于关闭状态，无法申购");
                return;
            }

            if (!product.getCoin().equalsIgnoreCase(coin.getName())) {
                log.error("申购产品币别类型与充值币别不符合");
                return;
            }

            if (orderAdvance.getAmount().compareTo(finalAmount) != 0) {
                log.error("充值金额与预订单金额不符合");
                return;
            }

            webHookService.dingTalkSend("监测到预购订单消费事件" + req.getHash() + ",时间：" + LocalDateTime.now());

            Order order = orderService.getByOrderNo(AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId());

            if (ProductType.fund.equals(product.getType())) {
                WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(productId);
                FundPurchaseBO fundPurchaseBO = new FundPurchaseBO();
                fundPurchaseBO.setProductId(orderAdvance.getProductId());
                fundPurchaseBO.setReferralCode(walletAgentProduct.getReferralCode());
                fundPurchaseBO.setPurchaseAmount(orderAdvance.getAmount());
                fundProductService.purchase(uid, fundPurchaseBO, order);
            }

            if (!ProductType.fund.equals(product.getType())) {
                PurchaseQuery purchaseQuery = PurchaseQuery.builder()
                        .coin(orderAdvance.getCoin())
                        .term(orderAdvance.getTerm())
                        .amount(orderAdvance.getAmount())
                        .autoCurrent(orderAdvance.isAutoCurrent())
                        .productId(orderAdvance.getProductId()).build();
                financialProductService.purchase(uid, purchaseQuery, order);
            }

            // 预订单状态设置为完成
            orderAdvance.setFinish(1);
            baseMapper.updateById(orderAdvance);

            order.setStatus(ChargeStatus.chain_success);
            order.setCompleteTime(LocalDateTime.now());
            orderService.updateById(order);
        } catch (Exception e) {
            webHookService.dingTalkSend("预购订单异常", e);
        }
    }

    /**
     * 增加尝试的次数
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTryTimes(Long id) {
        baseMapper.addTryTimes(id);
    }

}
