package com.tianli.exchange.push;

import com.tianli.exchange.entity.KLinesInfo;
import lombok.Builder;

/**
 * @author lzy
 * @date 2022/6/15 16:18
 */
@Builder
public class WebSocketEventType {

    private String e;

    private Long E;

    private String s;

    private KLineStream k;

    public static WebSocketEventType getWebSocketEventType(KLinesInfo kLinesInfo, Long time) {
        KLineStream kLineStream = KLineStream.builder()
                .t(kLinesInfo.getOpening_time())
                .T(kLinesInfo.getClosing_time())
                .s(kLinesInfo.getSymbol())
                .i(kLinesInfo.getInterval())
                .f(kLinesInfo.getOpening_trade_id())
                .L(kLinesInfo.getClosing_trade_id())
                .o(kLinesInfo.getOpening_price())
                .c(kLinesInfo.getClosing_price())
                .h(kLinesInfo.getHighest_price())
                .l(kLinesInfo.getLowest_price())
                .v(kLinesInfo.getVolume())
                .n(kLinesInfo.getTurnover_num())
                .x(Boolean.FALSE)
                .q(kLinesInfo.getTurnover())
                .V(kLinesInfo.getActive_buy_volume())
                .Q(kLinesInfo.getActive_buy_turnover())
                .build();
        return WebSocketEventType.builder()
                .e("kline")
                .E(time)
                .s(kLinesInfo.getSymbol())
                .k(kLineStream)
                .build();
    }
}
