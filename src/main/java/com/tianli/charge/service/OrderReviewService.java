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
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.service.HotWalletDetailedService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Transactional
    public void review(OrderReviewQuery query) {
        var orderNo = query.getOrderNo();
        Order order = orderService.getByOrderNo(query.getOrderNo());

        validReviewOrder(orderNo, order);

        // 订单详情
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
        Coin coin = coinService.getByNameAndNetwork(orderChargeInfo.getCoin(), orderChargeInfo.getNetwork());
        NetworkType network = orderChargeInfo.getNetwork();

        // 判断热钱包余额是充足(如果不足则改成人工审核)
        BigDecimal balance = contractAdapter.getBalance(network, coin);
        if (balance.compareTo(orderChargeInfo.getFee()) < 0) {
            webHookService.dingTalkSend("热钱包余额不足：" + network.name() + ":" + coin.getName() + "钱包余额：" + balance.stripTrailingZeros().toPlainString() +
                    " 提币金额：" + orderChargeInfo.getFee().stripTrailingZeros().toPlainString());
            if (query.isAutoPass()) {
                return;
            } else {
                throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
            }
        }


        // 获取审核策略
        OrderReviewStrategy strategy = query.isAutoPass() ? OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER :
                withdrawReviewStrategy.getStrategy(order, orderChargeInfo);
        log.info("当前提现策略是 ： " + strategy.name());
        // 人工审核人工打币判断
        if (OrderReviewStrategy.MANUAL_REVIEW_MANUAL_TRANSFER.equals(strategy)
                && StringUtils.isBlank(query.getHash()) && query.isPass()) {
            ErrorCodeEnum.MANUAL_TRANSFER_HASH_NULL.throwException();
        }

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

        order.setReviewerId(orderReview.getId());

        // 审核通过需要上链 如果传入的hash值为空说明是自动转账
        if ((OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy) || OrderReviewStrategy.MANUAL_REVIEW_AUTO_TRANSFER.equals(strategy)) &&
                query.isPass() && StringUtils.isBlank(query.getHash())) {
            chargeService.withdrawChain(order);
            order.setStatus(ChargeStatus.chaining);
            orderService.saveOrUpdate(order);
            // 上链数据通过回调操作，直接返回
            return;
        }

        // 手动打币
        if (OrderReviewStrategy.MANUAL_REVIEW_MANUAL_TRANSFER.equals(strategy) &&
                query.isPass() && StringUtils.isNotBlank(query.getHash())) {
            // 更新order相关链信息
            orderChargeInfo.setTxid(query.getHash());
            orderChargeInfoService.updateById(orderChargeInfo);
            withdrawSuccess(order, orderChargeInfo);
            return;
        }

        // 审核不通过需要解冻金额
        if (!query.isPass()) {
            accountBalanceServiceImpl.unfreeze(order.getUid(), ChargeType.withdraw, order.getCoin(), order.getAmount(), orderNo, "提现申请未通过");
            order.setStatus(ChargeStatus.review_fail);
            order.setCompleteTime(LocalDateTime.now());
            orderService.saveOrUpdate(order);
        }

    }

    /**
     * 提现审核做校验
     */
    private void validReviewOrder(String orderNo, Order order) {
        if (Objects.isNull(order)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException("未找到对应的订单：" + orderNo);
        }

        if (!ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅限制提现订单能通过审核");
        }
        if (!ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅未审核订单可以通过审核");
        }
        if (Objects.nonNull(order.getReviewerId()) || !ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "已存在审核记录，reviewId: " + order.getReviewerId());
        }
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

}
