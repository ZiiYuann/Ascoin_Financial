package com.tianli.exchange.push;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.tianli.exchange.entity.ExchangeMarketData;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import com.tianli.exchange.enums.WebSocketChannel;
import com.tianli.tool.WebSocketUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Map;

/**
 * @author lzy
 * @date 2022/6/16 14:55
 */
@Component
public class QuotesPush {

    @Resource
    Gson gson;


    /**
     * 推送k线
     *
     * @param map
     * @param kLinesIntervalEnum
     */
    public void pushKLinesInfos(Map<String, KLinesInfo> map, KLinesIntervalEnum kLinesIntervalEnum) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        for (String symbol : map.keySet()) {
            KLinesInfo kLinesInfo = map.get(symbol);
            String channel = StrUtil.format(WebSocketChannel.k_lines_stream, symbol, kLinesIntervalEnum.getInterval());
            WebSocketStream webSocketStream = WebSocketStream.builder()
                    .stream(channel)
                    .data(WebSocketEventType.getWebSocketEventType(kLinesInfo, timeInMillis))
                    .build();
            String data = gson.toJson(webSocketStream);
            WebSocketUtils.convertAndSend(channel, data);
        }
    }

    public void push24HrInfos(KLinesInfo kLinesInfo) {
        MiniTickerStream miniTickerStream = MiniTickerStream.getMiniTickerStream(kLinesInfo);
        String channel = WebSocketChannel.ticker24Hr;
        WebSocketStream webSocketStream = WebSocketStream.builder()
                .stream(channel)
                .data(miniTickerStream)
                .build();
        WebSocketUtils.convertAndSend(channel, gson.toJson(webSocketStream));
    }

    /**
     * 推送逐笔交易
     *
     * @param marketData
     */
    public void pushTrade(ExchangeMarketData marketData) {
        TradeStream tradeStream = TradeStream.getTradeStream(marketData, Calendar.getInstance().getTimeInMillis());
        String channel = StrUtil.format(WebSocketChannel.trade_stream, marketData.getSymbol());
        WebSocketStream webSocketStream = WebSocketStream.builder()
                .stream(channel)
                .data(tradeStream)
                .build();
        String data = gson.toJson(webSocketStream);
        WebSocketUtils.convertAndSend(channel, data);
    }

    /**
     * 推送深度信息
     *
     * @param depthStream
     * @param symbol
     */
    public void pushDepth(DepthStream depthStream, String symbol) {
        String channel = StrUtil.format(WebSocketChannel.depth_stream, symbol);
        WebSocketStream webSocketStream = WebSocketStream.builder()
                .stream(channel)
                .data(depthStream)
                .build();
        String data = gson.toJson(webSocketStream);
        WebSocketUtils.convertAndSend(channel, data);
    }
}
