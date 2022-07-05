package com.tianli.exchange.service;

import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.dto.PlaceOrderDTO;

/**
 * @author lzy
 * @date 2022/5/24 14:00
 */
public interface OrderStrategy {

    void placeOrder(PlaceOrderDTO placeOrderDTO);

    void makeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO);

    /**
     * 撤销订单
     * @param currencyTokenOrder
     */
    void revokeOrder(CurrencyTokenOrder currencyTokenOrder,ExchangeCoreResponseConvertDTO coreResponseDTO);
}
