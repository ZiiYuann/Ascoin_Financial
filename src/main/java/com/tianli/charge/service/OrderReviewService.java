package com.tianli.charge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.entity.OrderReview;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.OrderReviewStrategy;
import com.tianli.charge.mapper.OrderReviewMapper;
import com.tianli.charge.query.OrderReviewQuery;
import com.tianli.charge.vo.OrderReviewVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.tool.ApplicationContextTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Slf4j
@Service
public class OrderReviewService extends ServiceImpl<OrderReviewMapper, OrderReview> {

    public OrderReviewVO getVOByOrderNo(String orderNo) {
        Order order = Optional.ofNullable(orderService.getByOrderNo(orderNo))
                .orElseThrow(() -> ErrorCodeEnum.ARGUEMENT_ERROR.generalException("未找到对应的订单：" + orderNo));

        Long reviewId = order.getReviewerId();
        if (Objects.isNull(reviewId)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "未找到相关的审核记录，请校验");
        }

        Long relatedId = order.getRelatedId();
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(relatedId);

        OrderReview orderReview = Optional.ofNullable(orderReviewMapper.selectById(reviewId))
                .orElseThrow(() -> ErrorCodeEnum.ARGUEMENT_ERROR.generalException("未找到对应的审核记录:" + reviewId));
        OrderReviewVO orderReviewVO = chargeConverter.toOrderReviewVO(orderReview);
        orderReviewVO.setTxid(orderChargeInfo.getTxid());
        return orderReviewVO;
    }

    public void review(OrderReviewQuery query) {
        Order order = orderService.getByOrderNo(query.getOrderNo());
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());

        this.validReviewOrder(order, orderChargeInfo);

        Coin coin = coinService.getByNameAndNetwork(orderChargeInfo.getCoin(), orderChargeInfo.getNetwork());
        if (!this.validHotWallet(query, coin, orderChargeInfo)) {
            return;
        }

        // 获取审核策略(自动审核直接使用枚举，否则会增加提现次数)
        OrderReviewStrategy strategy = query.isAutoPass() ? OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER :
                withdrawReviewStrategy.getStrategy(order, orderChargeInfo);
        this.validWithdrawStrategy(query, strategy);

        // 默认隔离级别取数据库 传播行为REQUIRED
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);

        ChargeStatus status = query.isPass() ? ChargeStatus.chaining : ChargeStatus.review_fail;
        LocalDateTime now = LocalDateTime.now();
        OrderReview orderReview = OrderReview.builder().id(CommonFunction.generalId())
                .remarks(query.getRemarks())
                .rid(query.getRid())
                .status(status)
                .reviewBy(query.getReviewBy())
                .type(strategy.getReviewType().getType())
                .createTime(now).build();
        orderReviewMapper.insert(orderReview);


        // 审核通过需要上链 如果传入的hash值为空说明是自动转账
        if ((OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy) || OrderReviewStrategy.MANUAL_REVIEW_AUTO_TRANSFER.equals(strategy)) &&
                query.isPass() && StringUtils.isBlank(query.getHash())) {
            orderService.reviewOrder(order.getOrderNo(), orderReview.getId());
            // 主动提交事务
            platformTransactionManager.commit(transactionStatus);

            // 可以接受充值操作失败，但是决定不允许提现多次
            // 最关键的提现操作 ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            String txid = chargeService.withdrawChain(order, orderChargeInfo);
            // 最关键的提现操作 ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！

            transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
            if (Objects.isNull(txid)) {
                orderService.reviewOrderRollback(order.getOrderNo());
            }
            if (Objects.nonNull(txid)) {
                orderChargeInfoService.updateTxid(orderChargeInfo.getId(), txid);
            }
            platformTransactionManager.commit(transactionStatus);

            return;
        }

        // 手动打币
        if (OrderReviewStrategy.MANUAL_REVIEW_MANUAL_TRANSFER.equals(strategy) &&
                query.isPass() && StringUtils.isNotBlank(query.getHash())) {
            // 更新order相关链信息
            orderChargeInfo.setTxid(query.getHash());
            orderChargeInfoService.updateById(orderChargeInfo);
            var bean = ApplicationContextTool.getBean(OrderReviewService.class);
            bean = Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
            bean.withdrawSuccess(order, orderChargeInfo);
            // 主动提交事务
            platformTransactionManager.commit(transactionStatus);
            return;
        }

        // 审核不通过需要解冻金额
        if (!query.isPass()) {
            accountBalanceServiceImpl.unfreeze(order.getUid(), ChargeType.withdraw, order.getCoin(), order.getAmount()
                    , order.getOrderNo(), "提现申请未通过");
            order.setStatus(ChargeStatus.review_fail);
            order.setCompleteTime(LocalDateTime.now());
            orderService.saveOrUpdate(order);
            // 主动提交事务
        }
        platformTransactionManager.commit(transactionStatus);
    }

    private void validWithdrawStrategy(OrderReviewQuery query, OrderReviewStrategy strategy) {
        log.info("当前提现策略是 ： " + strategy.name());
        // 人工审核人工打币判断
        if (OrderReviewStrategy.MANUAL_REVIEW_MANUAL_TRANSFER.equals(strategy)
                && StringUtils.isBlank(query.getHash()) && query.isPass()) {
            ErrorCodeEnum.MANUAL_TRANSFER_HASH_NULL.throwException();
        }
    }


    /**
     * 提现审核做校验
     */
    private void validReviewOrder(Order order, OrderChargeInfo orderChargeInfo) {
        String orderNo = order.getOrderNo();
        if (!ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅限制提现订单能通过审核");
        }
        if (!ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅未审核订单可以通过审核");
        }
        if (Objects.nonNull(order.getReviewerId()) || !ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "已存在审核记录，reviewId: " + order.getReviewerId());
        }

        if (Objects.nonNull(orderChargeInfo.getTxid())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException(String.format(
                    "当前订单：[%s]已经在：[%s] 网络存在交易hash：[%s]", order.getOrderNo(), orderChargeInfo.getNetwork(), orderChargeInfo.getTxid()));
        }
    }

    private boolean validHotWallet(OrderReviewQuery query, Coin coin, OrderChargeInfo orderChargeInfo) {
        // 判断热钱包余额是充足(如果不足则改成人工审核)
        BigDecimal balance = contractAdapter.getBalance(coin.getNetwork(), coin);
        if (Objects.isNull(query.getHash()) && query.isPass() && balance.compareTo(orderChargeInfo.getFee()) < 0) {
            webHookService.dingTalkSend("热钱包余额不足：" + coin.getNetwork().name() + ":" + coin.getName() + "钱包余额：" + balance.stripTrailingZeros().toPlainString() +
                    " 提币金额：" + orderChargeInfo.getFee().stripTrailingZeros().toPlainString());
            if (query.isAutoPass()) {
                // 余额不足则不能自动通过
                return false;
            } else {
                throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
            }
        }
        return true;
    }


    @Transactional
    public void withdrawSuccess(Order order, OrderChargeInfo orderChargeInfo) {
        // 操作余额信息
        accountBalanceServiceImpl.reduce(order.getUid(), ChargeType.withdraw, order.getCoin()
                , orderChargeInfo.getNetwork(), orderChargeInfo.getFee(), order.getOrderNo(), "提现成功扣除");

        // 插入热钱包操作数据表
        HotWalletDetailed hotWalletDetailed = HotWalletDetailed.builder()
                .id(CommonFunction.generalId())
                .uid(orderChargeInfo.getUid() + "")
                .amount(orderChargeInfo.getFee())
                .coin(orderChargeInfo.getCoin())
                .chain(orderChargeInfo.getNetwork().getChainType())
                .fromAddress(orderChargeInfo.getFromAddress())
                .toAddress(orderChargeInfo.getToAddress())
                .hash(orderChargeInfo.getTxid())
                .type(HotWalletOperationType.user_withdraw)
                .createTime(LocalDateTime.now()).build();
        hotWalletDetailedService.insert(hotWalletDetailed);

        order.setStatus(ChargeStatus.chain_success);
        order.setCompleteTime(LocalDateTime.now());
        orderService.saveOrUpdate(order);
    }

    @Resource
    private ChargeConverter chargeConverter;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderReviewMapper orderReviewMapper;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private HotWalletDetailedService hotWalletDetailedService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private WithdrawReviewStrategy withdrawReviewStrategy;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private CoinService coinService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

}
