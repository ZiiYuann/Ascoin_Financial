package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.openapi.IdVO;
import com.tianli.openapi.entity.OrderRewardRecord;
import com.tianli.openapi.query.OpenapiOperationQuery;
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
    public IdVO reward(OpenapiOperationQuery query) {
        if (!ChargeType.transaction_reward.equals(query.getType())) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }

        OrderRewardRecord rewardRecord = orderRewardRecordService.lambdaQuery()
                .eq(OrderRewardRecord::getOrderId, query.getId()).one();
        if (Objects.nonNull(rewardRecord)) {
            return new IdVO(rewardRecord.getId());
        }

        OrderRewardRecord orderRewardRecord = insertOrderRecord(query);
        long recordId = orderRewardRecord.getId();
        LocalDateTime orderDateTime = orderRewardRecord.getGiveTime();

        LocalDateTime hour = TimeTool.hour(orderDateTime);
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getType, query.getType())
                .eq(Order::getUid, query.getUid())
                .eq(Order::getCoin, query.getCoin())
                .eq(Order::getCreateTime, hour));


        if (Objects.nonNull(order)) {
            orderService.addAmount(order.getId(), query.getAmount());
        } else {
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
        return new IdVO(recordId);
    }

    @Transactional
    public IdVO transfer(OpenapiOperationQuery query) {
        if (!ChargeType.transfer_increase.equals(query.getType()) && !ChargeType.transfer_reduce.equals(query.getType())) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }

        OrderRewardRecord rewardRecord = orderRewardRecordService.lambdaQuery()
                .eq(OrderRewardRecord::getOrderId, query.getId())
                .eq(OrderRewardRecord::getType, query.getType())
                .one();
        if (Objects.nonNull(rewardRecord)) {
            return new IdVO(rewardRecord.getId());
        }

        OrderRewardRecord orderRewardRecord = insertOrderRecord(query);

        long id = CommonFunction.generalId();
        LocalDateTime now = LocalDateTime.now();
        Order newOrder = Order.builder()
                .id(id)
                .uid(query.getUid())
                .coin(query.getCoin())
                .orderNo(query.getType().getAccountChangeType().getPrefix() + id)
                .amount(query.getAmount())
                .type(query.getType())
                .status(ChargeStatus.chain_success)
                .relatedId(query.getId())
                .createTime(now)
                .completeTime(now)
                .build();
        orderService.save(newOrder);

        if (ChargeType.transfer_increase.equals(query.getType())) {
            accountBalanceService.increase(query.getUid(), query.getType(), query.getCoin()
                    , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
        }

        if (ChargeType.transfer_reduce.equals(query.getType())) {
            accountBalanceService.decrease(query.getUid(), query.getType(), query.getCoin()
                    , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
        }

        return new IdVO(orderRewardRecord.getId());
    }

    /**
     * 记录order的操作记录（这里可以保证插入的幂等）
     *
     * @param query 请求
     * @return 记录
     */
    private OrderRewardRecord insertOrderRecord(OpenapiOperationQuery query) {
        LocalDateTime orderDateTime = TimeTool.getDateTimeOfTimestamp(query.getGive_time());
        long recordId = CommonFunction.generalId();
        OrderRewardRecord orderRewardRecord = OrderRewardRecord.builder()
                .id(recordId)
                .coin(query.getCoin())
                .amount(query.getAmount())
                .type(query.getType())
                .uid(query.getUid())
                .giveTime(orderDateTime)
                .orderId(query.getId())
                .build();
        orderRewardRecordService.save(orderRewardRecord);
        return orderRewardRecord;
    }
}
