package com.tianli.exchange.service;

import com.tianli.exchange.dto.ExchangeCoreResponseDTO;
import com.tianli.exchange.dto.ExchangeDepthDTO;
import com.tianli.exchange.dto.PlaceOrderDTO;
import com.tianli.exchange.dto.RevokeOrderDTO;
import com.tianli.exchange.vo.DepthVo;

/**
 * @author lzy
 * @date 2022/5/24 11:42
 */
public interface IOrderService {
    /**
     * 下单
     * @param placeOrderDTO
     * @param uid
     */
    void placeOrder(PlaceOrderDTO placeOrderDTO, Long uid);

    /**
     * 撤销订单
     * @param revokeOrderDTO
     */
    void revokeOrder(RevokeOrderDTO revokeOrderDTO);

    /**
     * 撮合订单系统返回
     * @param coreResponseDTO
     */
    void matchOrder(ExchangeCoreResponseDTO coreResponseDTO);

    /**
     * 深度信息
     * @param exchangeDepthDTO
     */
    void depth(ExchangeDepthDTO exchangeDepthDTO);

    /**
     * 根据交易对查询深度信息
     * @param symbol
     * @return
     */
    DepthVo queryDepth(String symbol);
}
