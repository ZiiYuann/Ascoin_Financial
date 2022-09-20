package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderAdvanceService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.WebHookService;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.financial.service.FinancialProductService;
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

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@Component
public class OrderAdvanceTask {


    @Resource
    private OrderService orderService;
    @Resource
    private OrderAdvanceService orderAdvanceService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private WebHookService webHookService;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void incomeTasks() {
        List<Order> advanceOrders = Optional.ofNullable(orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getType, ChargeType.purchase)
                .likeLeft(Order::getOrderNo, AccountChangeType.advance_purchase.getPrefix()))).orElse(new ArrayList<>());

        advanceOrders.forEach(this::scanChainOperation);
    }

    @Transactional
    public void scanChainOperation(Order order) {
        // relatedId 是 order_advance 主键 和 financial_record 的主键
        Long relatedId = order.getRelatedId();

        OrderAdvance orderAdvance = orderAdvanceService.getById(relatedId);
        if (LocalDateTime.now().until(orderAdvance.getCreateTime(), ChronoUnit.MINUTES) > 5) {
            if (StringUtils.isBlank(orderAdvance.getTxid())) {
                orderAdvance.setFinish(2);
                orderAdvanceService.updateById(orderAdvance);
                order.setStatus(ChargeStatus.chain_fail);
                orderService.updateById(order);
                return;
            }
        }

        String txid = orderAdvance.getTxid();
        NetworkType network = orderAdvance.getNetwork();

        ContractOperation contract = contractAdapter.getOne(network);

        boolean success = contract.successByHash(txid);
        if (success) {
            webHookService.dingTalkSend("奇怪的申购订单" + orderAdvance.getTxid(), new RuntimeException());
            return;
        }

        if (!success) {
            if (orderAdvance.getTryTimes() == 2) {
                orderAdvance.setFinish(2);
                orderAdvanceService.updateById(orderAdvance);

                order.setStatus(ChargeStatus.chain_fail);
                orderService.updateById(order);
                return;
            }
            orderAdvanceService.addTryTimes(orderAdvance.getId());
        }


    }


}
