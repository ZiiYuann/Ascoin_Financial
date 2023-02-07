package com.tianli.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.mapper.AccountUserTransferMapper;
import com.tianli.account.service.AccountUserTransferService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.openapi.query.UserTransferQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@Service
public class AccountUserTransferServiceImpl extends ServiceImpl<AccountUserTransferMapper, AccountUserTransfer>
        implements AccountUserTransferService {

    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceService;

    @Override
    @Transactional
    public AccountUserTransfer transfer(UserTransferQuery query) {
        if (Objects.nonNull(query.getRelatedId())) {
            AccountUserTransfer accountUserTransfer = this.baseMapper.selectOne(new LambdaQueryWrapper<AccountUserTransfer>()
                    .eq(AccountUserTransfer::getExternalPk, query.getRelatedId()));
            Optional.ofNullable(accountUserTransfer).ifPresent(o -> ErrorCodeEnum.TRANSFER_ERROR.throwException());
        }
        LocalDateTime now = LocalDateTime.now();
        long accountUserTransferId = CommonFunction.generalId();
        long tId = CommonFunction.generalId();
        long rId = CommonFunction.generalId();
        String s = CommonFunction.generalSn(tId);

        String coin = query.getCoin();
        BigDecimal amount = query.getAmount();

        Order transferOrder = Order.builder()
                .id(tId)
                .uid(query.getTransferUid())
                .orderNo(ChargeType.transfer_reduce.getAccountChangeType() + s)
                .type(ChargeType.transfer_reduce)
                .status(ChargeStatus.chain_success)
                .coin(coin)
                .amount(amount)
                .relatedId(accountUserTransferId)
                .completeTime(now).build();
        Order receiveOrder = Order.builder()
                .id(rId)
                .uid(query.getReceiveUid())
                .orderNo(ChargeType.transfer_increase.getAccountChangeType() + s)
                .type(ChargeType.transfer_increase)
                .status(ChargeStatus.chain_success)
                .coin(coin)
                .amount(amount)
                .relatedId(accountUserTransferId)
                .completeTime(now).build();

        orderService.save(transferOrder);
        orderService.save(receiveOrder);

        accountBalanceService.decrease(query.getTransferUid(), ChargeType.transfer_reduce, coin, amount
                , transferOrder.getOrderNo(), "划转减少");
        accountBalanceService.increase(query.getReceiveUid(), ChargeType.transfer_increase, coin, amount
                , receiveOrder.getOrderNo(), "划转增加");

        AccountUserTransfer accountUserTransfer = AccountUserTransfer.builder()
                .id(accountUserTransferId)
                .transferUid(query.getTransferUid())
                .receiveUid(query.getReceiveUid())
                .amount(amount)
                .coin(coin)
                .transferOrderNo(transferOrder.getOrderNo())
                .externalPk(query.getRelatedId()).build();
        this.save(accountUserTransfer);
        return accountUserTransfer;
    }
}
