package com.tianli.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.enums.RelatedRemarks;
import com.tianli.account.mapper.AccountUserTransferMapper;
import com.tianli.account.service.AccountBalanceService;
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
    private AccountBalanceService accountBalanceService;
    @Resource
    private AccountConverter accountConverter;

    public AccountUserTransfer getByExternalPk(Long id) {

        return this.getOne(new LambdaQueryWrapper<AccountUserTransfer>()
                .eq(AccountUserTransfer::getId, id));

    }

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

        if (ChargeType.user_credit_in.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.credit_out,
                    query.getReceiveUid(), ChargeType.user_credit_in,
                    coin, amount, null);
        }

        if (ChargeType.user_credit_out.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.user_credit_out,
                    query.getReceiveUid(), ChargeType.credit_in,
                    coin, amount, null);
        }

        if (ChargeType.transfer_reduce.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.transfer_reduce,
                    query.getReceiveUid(), ChargeType.transfer_increase,
                    coin, amount, RelatedRemarks.USER_TRANSFER.name());
        }

        //ID转账
        if (ChargeType.withdraw_success.equals(query.getChargeType())) {
            orderNo = transferOperation(accountUserTransferId,
                    query.getTransferUid(), ChargeType.withdraw,
                    query.getReceiveUid(), ChargeType.recharge,
                    coin, amount, RelatedRemarks.USER_TRANSFER.name());
        }

        orderNo = Optional.ofNullable(orderNo).orElseThrow(ErrorCodeEnum.TRANSFER_ERROR::generalException);

        AccountUserTransfer accountUserTransfer = AccountUserTransfer.builder()
                .id(accountUserTransferId)
                .transferUid(query.getTransferUid())
                .transferChatId(query.getTransferChatId())
                .receiveUid(query.getReceiveUid())
                .receiveChatId(query.getReceiveChatId())
                .amount(amount)
                .coin(coin)
                .transferOrderNo(orderNo)
                .externalPk(query.getRelatedId()).build();
        this.save(accountUserTransfer);
        return accountUserTransfer;
    }

    @Override
    public AccountUserTransferVO getVOByExternalPk(Long externalPk) {
        return accountConverter.toAccountUserTransferVO(this.getOne(new LambdaQueryWrapper<AccountUserTransfer>()
                .eq(AccountUserTransfer::getExternalPk, externalPk)));
    }

    @Override
    public AccountUserTransferVO getVOById(Long id) {
        return accountConverter.toAccountUserTransferVO(this.getOne(new LambdaQueryWrapper<AccountUserTransfer>()
                .eq(AccountUserTransfer::getId, id)));
    }

    private String transferOperation(
            Long accountUserTransferId,
            Long decreaseUid, ChargeType decreaseType,
            Long increaseUid, ChargeType increaseType,
            String coin, BigDecimal amount, String relatedRemarks) {
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
                .relatedRemarks(relatedRemarks)
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
                .relatedRemarks(relatedRemarks)
                .completeTime(now).build();

        orderService.save(transferOrder);
        orderService.save(receiveOrder);

        accountBalanceService.decrease(decreaseUid, decreaseType, coin, amount
                , transferOrder.getOrderNo());
        accountBalanceService.increase(increaseUid, increaseType, coin, amount
                , receiveOrder.getOrderNo());
        return transferOrder.getOrderNo();
    }
}
