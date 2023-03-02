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
import com.tianli.product.aborrow.query.RepayCoinQuery;
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
public class RepayAdvanceProcessor extends AbstractAdvanceProcessor<RepayCoinQuery> {

    @Resource
    private BorrowService borrowService;

    @Override
    public AdvanceType getType() {
        return AdvanceType.REPAY;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        RepayCoinQuery repayCoinQuery = query.getRepayCoinQuery();
        if (Objects.isNull(repayCoinQuery)){
            log.error("还币预订单参数异常");
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
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
    protected void handlerRechargerOperation(OrderAdvance orderAdvance, TRONTokenReq tronTokenReq, BigDecimal finalAmount, Coin coin) {
        RepayCoinQuery query = this.getQuery(orderAdvance);
        borrowService.repayCoin(orderAdvance.getUid(), query);
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
