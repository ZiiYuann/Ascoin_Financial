package com.tianli.exchange.push;

import com.tianli.exchange.entity.ExchangeMarketData;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/6/16 15:24
 */
@Builder
@Data
public class TradeStream {

    private Long id;

    private String symbol;

    private BigDecimal price;

    private BigDecimal qty;

    private Long time;

    public static TradeStream getTradeStream(ExchangeMarketData exchangeMarketData, long time) {
        return TradeStream.builder()
                .id(exchangeMarketData.getId())
                .symbol(exchangeMarketData.getSymbol())
                .price(exchangeMarketData.getPrice())
                .qty(exchangeMarketData.getVolume())
                .time(exchangeMarketData.getTransaction_time())
                .build();
    }
}
