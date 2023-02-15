package com.tianli.common;

public class RedisConstants {
    public static String CAPTCHA_CODE_KEY = "agent:captcha_codes:";

    public static String AGENT_SESSION_KEY = "agent:session:";


    /**
     *
     * RED_ENVELOPE_BLOOM：布隆过滤器RED_ENVELOPE：红包信息 【STRING】
     * RED_ENVELOPE_RECORD：领取记录信息 【ZSET】
     * RED_EXTERN：未兑换红包信息【ZSET】
     * RED_EXTERN_RECORD：未领取、已经兑换、过期汇总 【ZSET】
     * RED_EXTERN_CODE：兑换码与子红包内容 【STRING】
     * RED_ENVELOPE_LIMIT：指纹或者ip限制缓存 【STRING】防止重复领取
     * SPILT_RED_ENVELOPE 拆分红包id缓存 【SET】
     * SPILT_RED_ENVELOPE_GET 领取记录 【STRING】 防止重复领取
     *
     */
    public static String SPILT_RED_ENVELOPE = "red:spilt:";
    public static String SPILT_RED_ENVELOPE_GET = "red:spilt:get:";
    public static String RED_EXTERN_CODE = "red:extern:code:";
    // score 如果是时间搓代表兑换码过期时间
    public static String RED_EXTERN = "red:extern:";
    // score 如果是时间搓代表兑换码领取时间
    public static String RED_EXTERN_RECORD = "red:extern:record:";
    public static String RED_ENVELOPE = "red:";
    public static String RED_ENVELOPE_LIMIT = "red:limit:";

    public static String RECOMMEND_PRODUCT = "recommend:list";

    public static String RED_ENVELOPE_RECORD = "red:record:";

    public static String ACCOUNT_TRANSACTION_TYPE = "account:transaction:type";

    public static String COIN_BASE_LIST = "coin:base:list";

    public static String COIN_BASE = "coin:base:";

    public static String COIN_PUSH_LIST = "coin:push:list";

    public static final String USER_WITHDRAW_LIMIT = "user:withdraw:limit:";

    public static final String WITHDRAW_BLACK = "withdraw:black";

    public static final String HOT_WALLET_BALANCE = "hot:wallet:balance";

}
