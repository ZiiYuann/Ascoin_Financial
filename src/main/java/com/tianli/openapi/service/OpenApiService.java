package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.openapi.IdVO;
import com.tianli.openapi.entity.OrderRewardRecord;
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
    private AccountBalanceService accountBalanceService;
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


    /**
     * 获取统计数据
     *
     * @param chatId 聊天id
     * @return 统计数据
     */
    public StatisticsData accountData(Long chatId) {

        InviteDTO user = rpcService.inviteRpc(chatId);
        var subUids = user.getList().stream().map(InviteDTO::getUid).collect(Collectors.toList());
        Long uid = user.getUid();

        return getStatisticsData(uid, subUids);
    }

    private StatisticsData getStatisticsData(Long uid, List<Long> subUids) {
        var totalSummaryData = accountBalanceService.accountBalanceVOS(uid);
        // 总余额
        BigDecimal balance = totalSummaryData.stream()
                .map(AccountBalanceVO::getDollarBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rechargeAmount = orderService.uAmount(uid, ChargeType.recharge);
        BigDecimal withdrawAmount = orderService.uAmount(uid, ChargeType.withdraw);
        BigDecimal purchaseAmount = orderService.uAmount(uid, ChargeType.purchase);
        BigDecimal redeemAmount = orderService.uAmount(uid, ChargeType.redeem);

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
            Map<Long, BigDecimal> summaryBalanceAmount = accountBalanceService.getSummaryBalanceAmount(subUids);
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
            StatisticsData statisticsData = getStatisticsData(inviteDTO.getUid(), null);
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
        BigDecimal incomeAmount =
                financialIncomeAccrueService.summaryIncomeByQuery(FinancialProductIncomeQuery.builder().uids(subUids).build());
        Map<Long, BigDecimal> balanceMap = accountBalanceService.getSummaryBalanceAmount(subUids);
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

        result.setTotal(subUids.size());
        result.setRecords(resultRecords);
        return result;

    }


}
