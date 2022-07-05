package com.tianli.kline;

public class KLineConstants {

    /**
     * 默认抓取的kline量
     */
    public static final Integer KLINE_DEFAULT_SIZE = 500;

    /**
     * k线的缓存redis缓存KEY
     */
    public static final String KLINE_CACHE_PREFIX = "kline:cache:";

    /**
     * 火币 host
     */
    public static final String KLINE_HOST = "https://api.huobi.pro";

    /**
     * 火币的k线path
     */
    public static final String KLINE_PATH = "/market/history/kline";

    /**
     * 币安 host
     * https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT
     */
    public static final String BIAN_KLINE_HOST = "https://api.binance.com";

    /**
     * 币安最新价格path
     */
    public static final String BIAN_KLINE_PATH = "/api/v3/klines";

    /**
     * 币安最新价格path
     */
    public static final String BIAN_KLINE_PRICE_PATH = "/api/v3/ticker/price";

    /**
     * 币安24小时价格变动
     */
    public static final String BIAN_KLINE_24HR_PATH = "/api/v3/ticker/24hr";

    /**
     * 火币交易path
     */
    public static final String TRADE_PATH = "/market/trade";

    /**
     * MACD缓存 redis KEY
     */
    public static final String MACD_CACHE_REDIS_KEY = "MACD_CACHE_KEY";

    /**
     * 远程缓存KEY
     * redis缓存的押注信息的key值
     */
    public static final String REMOTE_BET_CACHE_KEY = "FSBA_REMOTE_CACHE_KEY";

    /**
     * 远程缓存KEY
     * redis缓存统计之后的结果信息的key值
     */
    public static final String STAT_DATA_CACHE_KEY = "STAT_DATA_CACHE_KEY";

}
