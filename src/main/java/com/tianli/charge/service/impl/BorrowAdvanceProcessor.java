package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class BorrowAdvanceProcessor extends AbstractAdvanceProcessor<BorrowCoinQuery> {
    @Override
    public AdvanceType getType() {
        return AdvanceType.BORROW;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        Optional.ofNullable(query.getBorrowCoinQuery())
                .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getBorrowCoinQuery()));
    }

    @Override
    protected ChargeType chargeType() {
        return ChargeType.borrow;
    }

    @Override
    protected Order generateOrder(OrderAdvance orderAdvance) {
        BorrowCoinQuery query = this.getQuery(orderAdvance);
        return Order.generate(orderAdvance.getUid(),
                this.chargeType(), query.getBorrowCoin(), query.getBorrowAmount(), orderAdvance.getId());
    }

    @Override
    public BorrowCoinQuery getQuery(OrderAdvance orderAdvance) {
        return JSONUtil.toBean(orderAdvance.getQuery(), BorrowCoinQuery.class);
    }
}
