package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.chain.enums.TransactionStatus;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.service.OrderAdvanceService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afund.contant.FundTransactionStatus;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.service.FinancialProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@Component
public class OrderTask {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderAdvanceService orderAdvanceService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private WebHookService webHookService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;
    @Resource
    private RedissonClientTool redissonClientTool;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void advanceTask() {
        redissonClientTool.tryLock(RedisLockConstants.ORDER_ADVANCE
                , () -> {
                    List<Order> advanceOrders = Optional.ofNullable(orderService.list(new LambdaQueryWrapper<Order>()
                            .eq(Order::getStatus, ChargeStatus.chaining)
                            .likeRight(Order::getOrderNo, AccountChangeType.advance_purchase.getPrefix()))).orElse(new ArrayList<>());
                    advanceOrders.forEach(this::scanChainOperation);
                }
                , ErrorCodeEnum.SYSTEM_BUSY, 1, TimeUnit.MINUTES, false);
    }

    @Transactional
    public void scanChainOperation(Order order) {
        // relatedId 是 order_advance 主键 和 financial_record 的主键
        Long relatedId = order.getRelatedId();

        OrderAdvance orderAdvance = orderAdvanceService.getById(relatedId);
        if (orderAdvance.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) < 20) {
            return;
        }

        webHookService.dingTalkSend("检测到异常申购预订单" + order.getOrderNo() + "  ,"
                + orderAdvance.getNetwork() + ":" + orderAdvance.getTxid());


        Long productId = orderAdvance.getProductId();
        FinancialProduct product = financialProductService.getById(productId);

        // 基金订单处理
        if (ProductType.fund.equals(product.getType())) {
            FundTransactionRecord fundTransactionRecord = fundTransactionRecordService.getById(order.getRelatedId());
            fundTransactionRecord.setStatus(FundTransactionStatus.fail);
            order.setCompleteTime(LocalDateTime.now());
            fundTransactionRecordService.updateById(fundTransactionRecord);
        }

        if (StringUtils.isNotBlank(orderAdvance.getTxid())) {
            String txid = orderAdvance.getTxid();
            NetworkType network = orderAdvance.getNetwork();
            ContractOperation contract = contractAdapter.getOne(network);
            TransactionStatus transactionStatus = contract.successByHash(txid);
            if (TransactionStatus.SUCCESS.equals(transactionStatus)) {
                webHookService.dingTalkSend("奇怪的申购订单" + orderAdvance.getTxid(), new RuntimeException());
            }
        }

        orderAdvance.setFinish(2);
        orderAdvanceService.updateById(orderAdvance);
        order.setStatus(ChargeStatus.chain_fail);
        order.setCompleteTime(LocalDateTime.now());
        orderService.updateById(order);
    }

}



