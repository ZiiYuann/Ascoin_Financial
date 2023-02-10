package com.tianli.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.mapper.AccountUserTransferMapper;
import com.tianli.account.service.AccountUserTransferService;
import com.tianli.account.vo.AccountUserTransferVO;
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
    @Resource
    private AccountConverter accountConverter;

    @Override
    @Transactional
    public AccountUserTransfer transfer(UserTransferQuery query) {
        if (Objects.nonNull(query.getRelatedId())) {
            AccountUserTransfer accountUserTransfer = this.baseMapper.selectOne(new LambdaQueryWrapper<AccountUserTransfer>()
                    .eq(AccountUserTransfer::getExternalPk, query.getRelatedId()));
            Optional.ofNullable(accountUserTransfer).ifPresent(o -> ErrorCodeEnum.TRANSFER_ORDER_EXIST.throwException());
        }
        long accountUserTransferId = CommonFunction.generalId();
        String coin = query.getCoin();
        BigDecimal amount = query.getAmount();

        String orderNo = null;

        if (ChargeType.points_sale.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.points_payment,
                    query.getReceiveUid(), ChargeType.points_sale,
                    coin, amount);
        }

        if (ChargeType.points_withdrawal.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.points_withdrawal,
                    query.getReceiveUid(), ChargeType.points_return,
                    coin, amount);
        }

        if (ChargeType.transfer_reduce.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.transfer_reduce,
                    query.getReceiveUid(), ChargeType.transfer_increase,
                    coin, amount);
        }

        Optional.ofNullable(orderNo).orElseThrow(ErrorCodeEnum.TRANSFER_ERROR::generalException);

        AccountUserTransfer accountUserTransfer = AccountUserTransfer.builder()
                .id(accountUserTransferId)
                .transferUid(query.getTransferUid())
                .receiveUid(query.getReceiveUid())
                .amount(amount)
                .coin(coin)
                .transferOrderNo(orderNo)
                .externalPk(query.getRelatedId()).build();
        this.save(accountUserTransfer);
        return accountUserTransfer;
    }

    @Override
    public AccountUserTransferVO getVO(Long transferId) {
        return accountConverter.toAccountUserTransferVO(this.getById(transferId));
    }

    private String transferOperation(
            Long accountUserTransferId,
            Long decreaseUid, ChargeType decreaseType,
            Long increaseUid, ChargeType increaseType,
            String coin, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        long tId = CommonFunction.generalId();
        long rId = CommonFunction.generalId();
        String s = CommonFunction.generalSn(tId);
        Order transferOrder = Order.builder()
                .id(tId)
                .uid(decreaseUid)
                .orderNo(decreaseType.getAccountChangeType().getPrefix() + s)
                .type(decreaseType)
                .status(ChargeStatus.chain_success)
                .coin(coin)
                .amount(amount)
                .relatedId(accountUserTransferId)
                .completeTime(now).build();

        Order receiveOrder = Order.builder()
                .id(rId)
                .uid(increaseUid)
                .orderNo(increaseType.getAccountChangeType().getPrefix() + s)
                .type(increaseType)
                .status(ChargeStatus.chain_success)
                .coin(coin)
                .amount(amount)
                .relatedId(accountUserTransferId)
                .completeTime(now).build();

        orderService.save(transferOrder);
        orderService.save(receiveOrder);

        accountBalanceService.decrease(decreaseUid, decreaseType, coin, amount
                , transferOrder.getOrderNo(), decreaseType.getNameZn());
        accountBalanceService.increase(increaseUid, increaseType, coin, amount
                , receiveOrder.getOrderNo(), increaseType.getNameZn());
        return transferOrder.getOrderNo();
    }
}
