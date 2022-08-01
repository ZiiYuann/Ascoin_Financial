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
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

        OrderReview orderReview = Optional.ofNullable(orderReviewMapper.selectById(reviewId))
                .orElseThrow(() -> ErrorCodeEnum.ARGUEMENT_ERROR.generalException("未找到对应的审核记录:" + reviewId));

        return chargeConverter.toOrderReviewVO(orderReview);
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
            chargeService.withdrawChain(order);
            order.setStatus(ChargeStatus.chaining);
        }
        // 审核不通过需要解冻金额
        if (!query.isPass()) {
            order.setStatus(ChargeStatus.review_fail);
            accountBalanceService.unfreeze(order.getUid(), ChargeType.withdraw, order.getCoin(), order.getAmount(), orderNo, "提现申请未通过");
        }

        orderService.saveOrUpdate(order);
    }

    @Resource
    private ChargeConverter chargeConverter;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderReviewMapper orderReviewMapper;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;


}
