package com.tianli.charge.service;

import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.vo.OrderBaseVO;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
public interface OrderAdvanceProcessor<T> {

    AdvanceType getType();

    void verifier(GenerateOrderAdvanceQuery query);

    void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance);

    OrderBaseVO update(OrderAdvance orderAdvance);

    T getQuery(OrderAdvance orderAdvance);

    void handlerRecharge(OrderAdvance orderAdvance, TRONTokenReq tronTokenReq, BigDecimal finalAmount, Coin coin);
}
