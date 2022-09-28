package com.tianli.charge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.entity.OrderReview;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderReviewMapper;
import com.tianli.charge.query.OrderReviewQuery;
import com.tianli.charge.vo.OrderReviewVO;
import com.tianli.common.CommonFunction;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.service.HotWalletDetailedService;
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
@Service
public class OrderReviewService extends ServiceImpl<OrderReviewMapper, OrderReview> {

    public OrderReviewVO getVOByOrderNo(String orderNo) {
        Order order = Optional.ofNullable(orderService.getOrderNo(orderNo))
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
        String orderNo = query.getOrderNo();
        Order order = Optional.ofNullable(orderService.getOrderNo(orderNo))
                .orElseThrow(() -> ErrorCodeEnum.ARGUEMENT_ERROR.generalException("未找到对应的订单：" + orderNo));

        if (!ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅限制提现订单能通过审核");
        }
        if (Objects.nonNull(order.getReviewerId()) || !ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "已存在审核记录，reviewId: " + order.getReviewerId());
        }
        ChargeStatus status = query.isPass() ? ChargeStatus.chaining : ChargeStatus.review_fail;
        LocalDateTime now = LocalDateTime.now();
        OrderReview orderReview = OrderReview.builder().id(CommonFunction.generalId())
                .remarks(query.getRemarks())
                .rid(1000000000L)
                .status(status)
                .createTime(now).build();
        orderReviewMapper.insert(orderReview);

        order.setReviewerId(orderReview.getId());

        // 审核通过需要上链
        if (query.isPass()) {
            OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
            // 如果传入的hash值为空说明是自动转账
            if (StringUtils.isBlank(query.getHash())) {
                BigDecimal uAmount = currencyService.getDollarRate(orderChargeInfo.getCoin()).multiply(orderChargeInfo.getFee());
                if (uAmount.compareTo(BigDecimal.valueOf(5000L)) > 0) {
                    ErrorCodeEnum.AUTO_PASS_ERROR.throwException();
                }
                chargeService.withdrawChain(order);
            } else {
                // 更新order相关链信息
                orderChargeInfo.setTxid(query.getHash());
                orderChargeInfoService.updateById(orderChargeInfo);
            }


            // 操作余额信息
            accountBalanceService.reduce(order.getUid(), ChargeType.withdraw, order.getCoin()
                    , orderChargeInfo.getNetwork(), orderChargeInfo.getFee(), order.getOrderNo(), "提现成功扣除");
            order.setStatus(ChargeStatus.chain_success);

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
        }

        // 审核不通过需要解冻金额
        if (!query.isPass()) {
            order.setStatus(ChargeStatus.review_fail);
            accountBalanceService.unfreeze(order.getUid(), ChargeType.withdraw, order.getCoin(), order.getAmount(), orderNo, "提现申请未通过");
        }

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
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private HotWalletDetailedService hotWalletDetailedService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private CurrencyService currencyService;

}
