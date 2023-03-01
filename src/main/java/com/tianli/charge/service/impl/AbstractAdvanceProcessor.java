package com.tianli.charge.service.impl;

import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.charge.service.OrderService;
import com.tianli.charge.vo.OrderBaseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-01
 **/
@Service
public abstract class AbstractAdvanceProcessor<T> implements OrderAdvanceProcessor<T> {

    @Resource
    private OrderService orderService;
    @Resource
    private ChargeService chargeService;

    protected abstract ChargeType chargeType();

    protected Order generateOrder(OrderAdvance orderAdvance) {
        return null;
    }

    @Override
    @Transactional
    public OrderBaseVO update(OrderAdvance orderAdvance) {
        Order order = this.generateOrder(orderAdvance);
        order.setStatus(ChargeStatus.chaining);
        order.setRelatedId(orderAdvance.getId());
        orderService.save(order);

        return null;
    }
}
