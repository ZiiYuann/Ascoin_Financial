package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderChargeInfoService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.webhook.WebHookService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-29
 **/
@Component
public class WithdrawOrderTask {

    @Resource
    private OrderService orderService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private AccountBalanceService accountBalanceService;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void withdrawTask() {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getType, ChargeType.withdraw)
                .eq(Order::getStatus, ChargeStatus.chaining);

        List<Order> orders = orderService.list(queryWrapper);
        orders.forEach(this::operation);
    }

    @Transactional
    public void operation(Order order) {

        Long relatedId = order.getRelatedId();
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(relatedId);
        if (Objects.isNull(orderChargeInfo) || StringUtils.isBlank(orderChargeInfo.getTxid())) {
            webHookService.dingTalkSend("异常提现订单:" + order.getOrderNo() + "不存在链信息，或者不存在txid");
            return;
        }
        if (orderChargeInfo.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) < 15) {
            return;
        }
        String txid = orderChargeInfo.getTxid();
        boolean success = contractAdapter.getOne(orderChargeInfo.getNetwork()).successByHash(txid);
        if (success) {
            webHookService.dingTalkSend("异常提现订单:" + order.getOrderNo() + " 此订单交易成功，但是订单状态未修改，请及时排除问题");
            return;
        }

        order.setStatus(ChargeStatus.chain_fail);
        order.setCompleteTime(LocalDateTime.now());
        orderService.updateById(order);

        accountBalanceService.unfreeze(order.getUid(), ChargeType.withdraw, order.getCoin(), order.getAmount(), order.getOrderNo()
                , "提现上链失败");

        webHookService.dingTalkSend("异常提现订单:" + order.getOrderNo() + " 修改订单状态为：fail");
    }


}
