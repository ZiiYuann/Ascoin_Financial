package com.tianli.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.vo.OrderBaseVO;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-22
 **/
public interface OrderAdvanceService extends IService<OrderAdvance> {

    Long generateOrderAdvance(GenerateOrderAdvanceQuery query);

    OrderBaseVO updateOrderAdvance(GenerateOrderAdvanceQuery query);

    void addTryTimes(Long id);

    void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, Coin coin);

    void finish(Long id);
}
