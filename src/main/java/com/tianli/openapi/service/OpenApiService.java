package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.service.AccountUserTransferService;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.AccountUserTransferVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.OrderMQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.openapi.dto.IdDto;
import com.tianli.openapi.dto.TransferResultDto;
import com.tianli.product.afinancial.service.FinancialIncomeAccrueService;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.openapi.entity.OrderRewardRecord;
import com.tianli.openapi.query.OpenapiAccountQuery;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.openapi.dto.StatisticsDataDto;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.InviteDTO;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private AccountUserTransferService accountUserTransferService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private WebHookService webHookService;

    @Transactional
    public IdDto reward(OpenapiOperationQuery query) {
        if (!ChargeType.transaction_reward.equals(query.getType())) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }

        OrderRewardRecord rewardRecord = orderRewardRecordService.lambdaQuery()
                .eq(OrderRewardRecord::getOrderId, query.getId()).one();
        if (Objects.nonNull(rewardRecord)) {
            return new IdDto(rewardRecord.getId());
        }

        OrderRewardRecord orderRewardRecord = insertOrderRecord(query);
        long recordId = orderRewardRecord.getId();
        LocalDateTime orderDateTime = orderRewardRecord.getGiveTime();

        LocalDateTime hour = TimeTool.hour(orderDateTime);
        String key = RedisLockConstants.LOCK_REWARD + query.getUid() + ":" + query.getCoin();
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean lock = rLock.tryLock(2L, TimeUnit.SECONDS);
            if (!lock) {
                webHookService.dingTalkSend("交易奖励获取锁超时" + key);
            }

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

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            assert rLock != null;
            rLock.unlock();
        }
        return new IdDto(recordId);
    }

    public TransferResultDto transfer(UserTransferQuery query) {
        TransferResultDto transferResultDto = new TransferResultDto();
        transferResultDto.setAmount(query.getAmount());
        transferResultDto.setId(accountUserTransferService.transfer(query).getId());
        return transferResultDto;
    }

    @Transactional
    public IdDto transfer(OpenapiOperationQuery query) {
        if (!ChargeType.transfer_increase.equals(query.getType())
                && !ChargeType.transfer_reduce.equals(query.getType())
                && !ChargeType.airdrop.equals(query.getType())
                && !ChargeType.swap_reward.equals(query.getType())) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }

        OrderRewardRecord rewardRecord = orderRewardRecordService.lambdaQuery()
                .eq(OrderRewardRecord::getOrderId, query.getId())
                .eq(OrderRewardRecord::getType, query.getType())
                .one();
        if (Objects.nonNull(rewardRecord)) {
            return new IdDto(rewardRecord.getId());
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

        if (ChargeType.transfer_increase.equals(query.getType())
                || ChargeType.airdrop.equals(query.getType()) || ChargeType.swap_reward.equals(query.getType())) {
            accountBalanceServiceImpl.increase(query.getUid(), query.getType(), query.getCoin()
                    , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
        }

        if (ChargeType.transfer_reduce.equals(query.getType())) {
            accountBalanceServiceImpl.decrease(query.getUid(), query.getType(), query.getCoin()
                    , query.getAmount(), newOrder.getOrderNo(), query.getType().getNameZn());
        }

        return new IdDto(orderRewardRecord.getId());
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
    public StatisticsDataDto accountData(OpenapiAccountQuery query) {
        InviteDTO user = rpcService.inviteRpc(query.getChatId(), query.getUid());
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

    private StatisticsDataDto getStatisticsData(Long uid, List<Long> subUids, LocalDateTime startTime, LocalDateTime endTime) {
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


        StatisticsDataDto data = StatisticsDataDto.builder().balance(balance)
                .redeemAmount(redeemAmount)
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .purchaseAmount(purchaseAmount)
                .incomeAmount(incomeAmount)
                .build();

        if (CollectionUtils.isNotEmpty(subUids)) {
            var allUserAssetsVO = accountBalanceServiceImpl.getAllUserAssetsVO(subUids);

            BigDecimal subRedeemAmount = orderService.uAmount(subUids, ChargeType.redeem);
            data.setSubBalance(allUserAssetsVO.getBalanceAmount());
            data.setSubPurchaseAmount(allUserAssetsVO.getPurchaseAmount());
            data.setSubRedeemAmount(subRedeemAmount);
            data.setUid(uid);
        }

        return data;
    }

    public IPage<StatisticsDataDto> accountSubData(Long chatId, Long uid, PageQuery<StatisticsDataDto> pageQuery) {
        InviteDTO user = rpcService.inviteRpc(chatId, uid);
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
        List<StatisticsDataDto> records = pageSub.stream().map(inviteDTO -> {
            StatisticsDataDto statisticsDataDto = getStatisticsData(inviteDTO.getUid(), null, null, null);
            statisticsDataDto.setUid(inviteDTO.getUid());
            statisticsDataDto.setChatId(inviteDTO.getChatId());
            return statisticsDataDto;
        }).collect(Collectors.toList());

        // 最终记录行
        List<StatisticsDataDto> resultRecords = new ArrayList<>();

        BigDecimal rechargeAmount = orderService.uAmount(subUids, ChargeType.recharge);
        BigDecimal withdrawAmount = orderService.uAmount(subUids, ChargeType.withdraw);
        BigDecimal purchaseAmount = orderService.uAmount(subUids, ChargeType.purchase);
        BigDecimal redeemAmount = orderService.uAmount(subUids, ChargeType.redeem);
        BigDecimal incomeAmount = CollectionUtils.isEmpty(subUids) ? BigDecimal.ZERO :
                financialIncomeAccrueService.summaryIncomeByQuery(FinancialProductIncomeQuery.builder().uids(subUids).build());
        var allUserAssetsVO = accountBalanceServiceImpl.getAllUserAssetsVO(subUids);

        Page<StatisticsDataDto> result = pageQuery.page();
        StatisticsDataDto firstRow = StatisticsDataDto.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .purchaseAmount(purchaseAmount)
                .redeemAmount(redeemAmount)
                .incomeAmount(incomeAmount)
                .balance(allUserAssetsVO.getBalanceAmount()).build();

        resultRecords.add(firstRow);
        resultRecords.addAll(records);

        result.setTotal(list.size());
        result.setRecords(resultRecords);
        return result;

    }


    public void returnGas(OpenapiOperationQuery query) {
        query.setType(ChargeType.return_gas);
        increaseBalance(query);
    }

    public void goldExchange(OpenapiOperationQuery query) {
        query.setType(ChargeType.gold_exchange);
        increaseBalance(query);
    }

    private void increaseBalance(OpenapiOperationQuery query) {
        LocalDateTime now = LocalDateTime.now();
        long id = CommonFunction.generalId();

        OrderRewardRecord orderRewardRecord = OrderRewardRecord.builder()
                .id(CommonFunction.generalId())
                .coin(query.getCoin())
                .amount(query.getAmount())
                .type(query.getType())
                .uid(query.getUid())
                .giveTime(now)
                .orderId(query.getId())
                .build();
        orderRewardRecordService.save(orderRewardRecord);

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

    public AccountUserTransferVO transferOrder(Long externalPk) {
        return accountUserTransferService.getVOByExternalPk(externalPk);
    }
}
