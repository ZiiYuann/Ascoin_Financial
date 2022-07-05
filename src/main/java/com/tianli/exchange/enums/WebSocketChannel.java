package com.tianli.exchange.enums;

/**
 * @author lzy
 * @date 2022/6/15 15:36
 */
public class WebSocketChannel {

    /**
     * 格式<symbol>@kline_<interval> 比如 BNBBTC@kline_1m
     */
    public static final String k_lines_stream = "{}@kline_{}";

    public static final String trade_stream = "{}@trade";

    public static final String depth_stream = "{}@depth";

    public static final String ticker24Hr = "ticker24Hr";
}
