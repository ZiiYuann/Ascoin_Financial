package com.tianli.common;

public class RedisConstants {
    public static String CAPTCHA_CODE_KEY = "agent:captcha_codes:";

    public static String AGENT_SESSION_KEY = "agent:session:";

    /**
     * 拆分红包
     */
    public static String SPILT_RED_ENVELOPE = "red:spilt:";

    /**
     * 拆分红包code获取（站外使用）
     */
    public static String RED_EXTERN_CODE = "red:extern:code:";

    // 拆分红包后会生成 score < 10000 代表是没生成过兑换码，如果
    public static String RED_EXTERN = "red:extern:";

    public static String RED_EXTERN_RECORD = "red:extern:record:";

    /**
     * 拆分红包领取
     */
    public static String SPILT_RED_ENVELOPE_GET = "red:spilt:get:";

    /**
     * 红包
     */
    public static String RED_ENVELOPE = "red:";

    public static String RED_ENVELOPE_LIMIT = "red:limit:";

    /**
     * 红包领取记录
     */
    public static String RED_ENVELOPE_GET_RECORD = "red:get:record:";

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
