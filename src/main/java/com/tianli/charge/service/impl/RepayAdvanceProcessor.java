package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class RepayAdvanceProcessor extends AbstractAdvanceProcessor<RepayCoinQuery> {
    @Override
    public AdvanceType getType() {
        return AdvanceType.REPAY;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        Optional.ofNullable(query.getRepayCoinQuery())
                .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getRepayCoinQuery()));
    }

    @Override
    protected ChargeType chargeType() {
        return ChargeType.repay;
    }

    @Override
    protected Order generateOrder(OrderAdvance orderAdvance) {
        RepayCoinQuery query = this.getQuery(orderAdvance);
        return Order.generate(orderAdvance.getId(),
                this.chargeType(), query.getCoin(), query.getAmount(), orderAdvance.getId());
    }

    @Override
    public RepayCoinQuery getQuery(OrderAdvance orderAdvance) {
        return JSONUtil.toBean(orderAdvance.getQuery(), RepayCoinQuery.class);
    }
}
