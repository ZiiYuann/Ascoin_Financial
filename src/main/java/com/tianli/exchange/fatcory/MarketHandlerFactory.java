package com.tianli.exchange.fatcory;

import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.handler.MarketHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @date 2022/6/13 14:44
 */
@Component
public class MarketHandlerFactory {

    final Map<String, MarketHandler> marketHandlerMap = new ConcurrentHashMap<>();

    public MarketHandlerFactory(Map<String, MarketHandler> marketHandlerMap) {
        marketHandlerMap.forEach(this.marketHandlerMap::put);
    }

    public void handleKLineStorage(KLinesInfo kLinesInfo) {
        for (MarketHandler value : marketHandlerMap.values()) {
            value.handleKLine(kLinesInfo);
        }
    }
}
