package com.tianli.exchange.fatcory;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.exception.ErrCodeException;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.dto.PlaceOrderDTO;
import com.tianli.exchange.service.OrderStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @date 2022/5/24 13:59
 * 订单工厂类
 */
@Component
public class OrderFactory {

    final Map<String, OrderStrategy> orderStrategyMap = new ConcurrentHashMap<>();

    final Map<TokenOrderType, String> serviceNames = new ConcurrentHashMap<>();

    public OrderFactory(Map<String, OrderStrategy> orderStrategyMap) {
        orderStrategyMap.forEach(this.orderStrategyMap::put);
        serviceNames.put(TokenOrderType.limit, "limitOrderService");
        serviceNames.put(TokenOrderType.market, "marketOrderService");
    }

    public OrderStrategy getStrategy(String serviceName) {
        OrderStrategy orderStrategy = orderStrategyMap.get(serviceName);
        if (ObjectUtil.isNull(orderStrategy)) {
            throw new ErrCodeException("no strategy defined");
        }
        return orderStrategy;
    }

    public void placeOrder(PlaceOrderDTO placeOrderDTO) {
        getStrategy(serviceNames.get(placeOrderDTO.getType())).placeOrder(placeOrderDTO);
    }

    public void makeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        getStrategy(serviceNames.get(currencyTokenOrder.getType())).makeOrder(currencyTokenOrder,coreResponseDTO);
    }

    public void revokeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        getStrategy(serviceNames.get(currencyTokenOrder.getType())).revokeOrder(currencyTokenOrder, coreResponseDTO);
    }

}
