package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.OrderMQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.openapi.IdVO;
import com.tianli.openapi.entity.OrderRewardRecord;
import com.tianli.openapi.query.OpenapiAccountQuery;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.vo.StatisticsData;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.InviteDTO;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private OrderRewardRecordService orderRewardRecordService;
    @Resource
    private RpcService rpcService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;

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

        accountBalanceServiceImpl.increase(query.getUid(), query.getType(), query.getCoin()
                , query.getAmount(), order.getOrderNo(), query.getType().getNameZn());
        return new IdVO(recordId);
    }

    @Transactional
    public IdVO transfer(OpenapiOperationQuery query) {
        if (!ChargeType.transfer_increase.equals(query.getType())
                && !ChargeType.transfer_reduce.equals(query.getType())
                && !ChargeType.airdrop.equals(query.getType())) {
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

        if (ChargeType.transfer_increase.equals(query.getType()) || ChargeType.airdrop.equals(query.getType())) {
            accountBalanceServiceImpl.increase(query.getUid(), query.getType(), query.getCoin()
                    , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
        }

        if (ChargeType.transfer_reduce.equals(query.getType())) {
            accountBalanceServiceImpl.decrease(query.getUid(), query.getType(), query.getCoin()
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


    /**
     * 获取统计数据
     *
     * @param query 请求参数
     * @return 统计数据
     */
    public StatisticsData accountData(OpenapiAccountQuery query) {

        InviteDTO user = rpcService.inviteRpc(query.getChatId());
        var subUids = user.getList().stream().map(InviteDTO::getUid).collect(Collectors.toList());
        Long uid = user.getUid();

        LocalDateTime startTime = query.getStartTime();
        LocalDateTime endTime = query.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        if ("day".equals(query.getTime())) {
            startTime = TimeTool.minDay(now);
            endTime = now;
        }

        if ("week".equals(query.getTime())) {
            Map<String, LocalDateTime> timeMap = TimeTool.thisWeekMondayToSunday();
            startTime = timeMap.get("start");
            endTime = timeMap.get("end");
        }

        if ("month".equals(query.getTime())) {
            startTime = TimeTool.minMonthTime(now);
            endTime = now;
        }

        return getStatisticsData(uid, subUids, startTime, endTime);
    }

    private StatisticsData getStatisticsData(Long uid, List<Long> subUids, LocalDateTime startTime, LocalDateTime endTime) {
        var totalSummaryData = accountBalanceServiceImpl.accountList(uid);
        // 总余额
        BigDecimal balance = totalSummaryData.stream()
                .map(AccountBalanceVO::getDollarBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderMQuery query = new OrderMQuery();
        query.setUid(uid);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        query.setType(ChargeType.recharge);
        BigDecimal rechargeAmount = orderService.uAmount(query);
        query.setType(ChargeType.withdraw);
        BigDecimal withdrawAmount = orderService.uAmount(query);
        query.setType(ChargeType.purchase);
        BigDecimal purchaseAmount = orderService.uAmount(query);
        query.setType(ChargeType.redeem);
        BigDecimal redeemAmount = orderService.uAmount(query);

        BigDecimal incomeAmount =
                financialIncomeAccrueService.summaryIncomeByQuery(FinancialProductIncomeQuery.builder().uid(uid + "").build());


        StatisticsData data = StatisticsData.builder().balance(balance)
                .redeemAmount(redeemAmount)
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .purchaseAmount(purchaseAmount)
                .incomeAmount(incomeAmount)
                .build();

        if (CollectionUtils.isNotEmpty(subUids)) {
            Map<Long, BigDecimal> summaryBalanceAmount = accountBalanceServiceImpl.getSummaryBalanceAmount(subUids);
            BigDecimal subBalance = summaryBalanceAmount.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal subPurchaseAmount = orderService.uAmount(subUids, ChargeType.purchase);
            BigDecimal subRedeemAmount = orderService.uAmount(subUids, ChargeType.redeem);
            data.setSubBalance(subBalance);
            data.setSubPurchaseAmount(subPurchaseAmount);
            data.setSubRedeemAmount(subRedeemAmount);
        }

        return data;
    }

    public IPage<StatisticsData> accountSubData(Long chatId, PageQuery<StatisticsData> pageQuery) {
        InviteDTO user = rpcService.inviteRpc(chatId);
        List<InviteDTO> list = Optional.ofNullable(user.getList()).orElse(new ArrayList<>());

        int start = (pageQuery.getPage() - 1) * pageQuery.getPageSize();
        int end = pageQuery.getPage() * pageQuery.getPageSize() - 1;

        List<InviteDTO> pageSub = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (i >= list.size()) {
                break;
            }
            pageSub.add(list.get(i));
        }
        var subUids = pageSub.stream().map(InviteDTO::getUid).collect(Collectors.toList());
        List<StatisticsData> records = pageSub.stream().map(inviteDTO -> {
            StatisticsData statisticsData = getStatisticsData(inviteDTO.getUid(), null, null, null);
            statisticsData.setUid(inviteDTO.getUid());
            statisticsData.setChatId(inviteDTO.getChatId());
            return statisticsData;
        }).collect(Collectors.toList());

        // 最终记录行
        List<StatisticsData> resultRecords = new ArrayList<>();

        BigDecimal rechargeAmount = orderService.uAmount(subUids, ChargeType.recharge);
        BigDecimal withdrawAmount = orderService.uAmount(subUids, ChargeType.withdraw);
        BigDecimal purchaseAmount = orderService.uAmount(subUids, ChargeType.purchase);
        BigDecimal redeemAmount = orderService.uAmount(subUids, ChargeType.redeem);
        BigDecimal incomeAmount = CollectionUtils.isEmpty(subUids) ? BigDecimal.ZERO :
                financialIncomeAccrueService.summaryIncomeByQuery(FinancialProductIncomeQuery.builder().uids(subUids).build());
        Map<Long, BigDecimal> balanceMap = accountBalanceServiceImpl.getSummaryBalanceAmount(subUids);
        BigDecimal balance = balanceMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        Page<StatisticsData> result = pageQuery.page();
        StatisticsData firstRow = StatisticsData.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .purchaseAmount(purchaseAmount)
                .redeemAmount(redeemAmount)
                .incomeAmount(incomeAmount)
                .balance(balance).build();

        resultRecords.add(firstRow);
        resultRecords.addAll(records);

        result.setTotal(list.size());
        result.setRecords(resultRecords);
        return result;

    }


    public void returnGas(OpenapiOperationQuery query) {
        query.setType(ChargeType.return_gas);
        LocalDateTime now = LocalDateTime.now();
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
                .createTime(now)
                .completeTime(now)
                .build();
        orderService.save(newOrder);

        accountBalanceServiceImpl.increase(query.getUid(), query.getType(), query.getCoin()
                , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
    }
}
