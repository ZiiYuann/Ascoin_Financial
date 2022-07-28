package com.tianli.charge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
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

    @Resource
    private ChargeConverter chargeConverter;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderReviewMapper orderReviewMapper;
    @Resource
    private RequestInitService requestInitService;

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

        if(!ChargeType.withdraw.equals(order.getType())){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "仅限制提现订单能通过审核");
        }
        if (Objects.nonNull(order.getReviewerId()) || !ChargeStatus.created.equals(order.getStatus())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException(orderNo + "已存在审核记录，reviewId: " +order.getReviewerId());
        }
        ChargeStatus status = query.isPass() ? ChargeStatus.chaining : ChargeStatus.review_fail;
        LocalDateTime now = LocalDateTime.now();
        OrderReview orderReview = OrderReview.builder().id(CommonFunction.generalId())
                .remarks(query.getRemarks())
                .rid(requestInitService.uid())
                .status(status)
                .createTime(now).build();
        orderReviewMapper.insert(orderReview);

        order.setStatus(status);
        order.setReviewerId(orderReview.getId());
        orderService.saveOrUpdate(order);

        if(query.isPass()){
            // todo 进行提现
        }
    }



}
