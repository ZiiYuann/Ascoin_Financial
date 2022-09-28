package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.charge.vo.OrderFundTransactionVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.fund.bo.FundPurchaseBO;
import com.tianli.fund.service.impl.FundRecordServiceImpl;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.sso.init.RequestInitService;
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
public class OrderAdvanceService extends ServiceImpl<OrderAdvanceMapper, OrderAdvance> {

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


    /**
     * 生成预订单
     */
    @Transactional
    @NoRepeatCommit(autoUnlock = false)
    public Long generateOrderAdvance(GenerateOrderAdvanceQuery query) {

        Long uid = requestInitService.uid();

        FinancialProduct product = financialProductService.getById(query.getProductId());

        if (ProductType.fund.equals(product.getType())) {
            WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(product.getId());
            if (!walletAgentProduct.getReferralCode().equals(query.getReferralCode())) {
                ErrorCodeEnum.REFERRAL_CODE_ERROR.throwException();
            }
        }

        OrderAdvance orderAdvance = OrderAdvance.builder()
                .id(CommonFunction.generalId())
                .amount(query.getAmount())
                .uid(uid)
                .createTime(LocalDateTime.now())
                .productId(query.getProductId())
                .coin(product.getCoin())
                .term(product.getTerm())
                .autoCurrent(query.isAutoCurrent())
                .build();
        baseMapper.insert(orderAdvance);

        return orderAdvance.getId();
    }

    /**
     * 更新预订单
     */
    @Transactional
    public OrderBaseVO updateOrderAdvance(GenerateOrderAdvanceQuery query) {
        OrderAdvance orderAdvance = baseMapper.selectById(query.getId());
        orderAdvance.setTxid(query.getTxid());
        orderAdvance.setNetwork(query.getNetwork());
        baseMapper.updateById(orderAdvance);

        Long uid = orderAdvance.getUid();
        Long productId = orderAdvance.getProductId();

        FinancialProduct product = financialProductService.getById(productId);

        // 生成一笔预订单记录
        Order order = Order.builder()
                .uid(requestInitService.uid())
                .coin(product.getCoin())
                .orderNo(AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId())
                .amount(orderAdvance.getAmount())
                .type(ChargeType.purchase)
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
            webHookService.dingTalkSend("监测到理财预购订单申购事件" + query.getTxid() + ",时间：" + LocalDateTime.now());
            return chargeService.orderDetails(requestInitService.uid()
                    , AccountChangeType.advance_purchase.getPrefix() + query.getId());

        }


        if (ProductType.fund.equals(product.getType())) {

            OrderFundTransactionVO orderFundTransactionVO = OrderFundTransactionVO.builder()
                    .status(2)
                    .createTime(order.getCreateTime())
                    .rate(product.getRate())
                    .coin(product.getCoin())
                    .id(orderAdvance.getId())
                    .transactionAmount(orderAdvance.getAmount())
                    .expectedIncome(fundRecordService.dailyIncome(orderAdvance.getAmount(), product.getRate())).build();

            webHookService.dingTalkSend("监测到基金预购订单申购事件" + query.getTxid() + ",时间：" + LocalDateTime.now());
            return orderFundTransactionVO;
        }

        return null;
    }

    /**
     * 处理充值事件
     */
    @Transactional
    public void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, TokenAdapter tokenAdapter) {

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

            if (!product.getCoin().equals(tokenAdapter.getCurrencyCoin())) {
                log.error("申购产品币别类型与充值币别不符合");
                return;
            }

            if (orderAdvance.getAmount().compareTo(finalAmount) != 0) {
                log.error("充值金额与预订单金额不符合");
                return;
            }

            webHookService.dingTalkSend("监测到预购订单消费事件" + req.getHash() + ",时间：" + LocalDateTime.now());

            PurchaseQuery purchaseQuery = PurchaseQuery.builder()
                    .coin(orderAdvance.getCoin())
                    .term(orderAdvance.getTerm())
                    .amount(orderAdvance.getAmount())
                    .autoCurrent(orderAdvance.isAutoCurrent())
                    .productId(orderAdvance.getProductId()).build();

            Order order = orderService.getOrderNo(AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId());

            if (ProductType.fund.equals(product.getType())) {
                WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(productId);
                FundPurchaseBO fundPurchaseBO = new FundPurchaseBO();
                fundPurchaseBO.setProductId(purchaseQuery.getProductId());
                fundPurchaseBO.setReferralCode(walletAgentProduct.getReferralCode());
                fundPurchaseBO.setPurchaseAmount(orderAdvance.getAmount());
                fundRecordService.purchase(uid, fundPurchaseBO, FundTransactionRecordVO.class, order);
            }

            if (!ProductType.fund.equals(product.getType())) {
                financialProductService.purchase(uid, purchaseQuery, FinancialPurchaseResultVO.class, order);
            }

            // 预订单状态设置为完成
            orderAdvance.setFinish(1);
            baseMapper.updateById(orderAdvance);

            order.setStatus(ChargeStatus.chain_success);
            orderService.updateById(order);
        } catch (Exception e) {
            webHookService.dingTalkSend("预购订单异常", e);
        }
    }

    /**
     * 增加尝试的次数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTryTimes(Long id) {
        baseMapper.addTryTimes(id);
    }

}
