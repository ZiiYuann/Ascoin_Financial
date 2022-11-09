package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.openapi.RewardVO;
import com.tianli.openapi.entity.OrderRewardRecord;
import com.tianli.openapi.query.RewardQuery;
import com.tianli.tool.time.TimeTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-24
 **/
@Service
public class OpenApiService {

    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderRewardRecordService orderRewardRecordService;

    @Transactional
    public RewardVO reward(RewardQuery query) {
        if (!ChargeType.transaction_reward.equals(query.getType())) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }

        OrderRewardRecord rewardRecord = orderRewardRecordService.lambdaQuery()
                .eq(OrderRewardRecord::getOrder_id, query.getId()).one();
        if (Objects.nonNull(rewardRecord)) {
            return new RewardVO(rewardRecord.getId());
        }

        LocalDateTime orderDateTime = TimeTool.getDateTimeOfTimestamp(query.getGive_time());
        long recordId = CommonFunction.generalId();
        OrderRewardRecord orderRewardRecord = OrderRewardRecord.builder()
                .id(recordId)
                .coin(query.getCoin())
                .amount(query.getAmount())
                .type(query.getType())
                .uid(query.getUid())
                .give_time(orderDateTime)
                .order_id(query.getId())
                .build();
        orderRewardRecordService.save(orderRewardRecord);

        LocalDateTime hour = TimeTool.hour(orderDateTime);
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getType, query.getType())
                .eq(Order::getUid, query.getUid())
                .eq(Order::getCoin, query.getCoin())
                .eq(Order::getCreateTime, hour));


        if (Objects.nonNull(order)) {
            orderService.addAmount(order.getId(), query.getAmount());
        }else {
            long id = CommonFunction.generalId();
            Order newOrder = Order.builder()
                    .id(id)
                    .uid(query.getUid())
                    .coin(query.getCoin())
                    .orderNo(query.getType().getAccountChangeType().getPrefix() + id)
                    .amount(query.getAmount())
                    .type(query.getType())
                    .status(ChargeStatus.chain_success)
                    .relatedId(query.getId())
                    .createTime(hour)
                    .completeTime(hour.plusHours(1).plusSeconds(-1))
                    .build();
            orderService.save(newOrder);
            order = newOrder;
        }

        accountBalanceService.increase(query.getUid(), query.getType(), query.getCoin()
                , query.getAmount(), order.getOrderNo(), query.getType().getNameZn());
        return new RewardVO(recordId);
    }

}
