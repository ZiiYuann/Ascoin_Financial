package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.service.BorrowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Slf4j
@Service
public class BorrowAdvanceProcessor extends AbstractAdvanceProcessor<BorrowCoinQuery> {

    @Resource
    private BorrowService borrowService;

    @Override
    protected ChargeType chargeType() {
        return ChargeType.borrow;
    }

    @Override
    public AdvanceType getType() {
        return AdvanceType.BORROW;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        if (Objects.isNull(query.getBorrowCoinQuery())) {
            log.error("借币预订单参数异常");
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getBorrowCoinQuery()));
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

    @Override
    protected void handlerRechargerOperation(OrderAdvance orderAdvance, TRONTokenReq tronTokenReq, BigDecimal finalAmount, Coin coin) {
        BorrowCoinQuery query = this.getQuery(orderAdvance);
        borrowService.borrowCoin(orderAdvance.getUid(), query);
    }
}
