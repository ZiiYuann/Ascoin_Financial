package com.tianli.common;

public class RedisConstants {
    public static String CAPTCHA_CODE_KEY = "agent:captcha_codes:";

    public static String AGENT_SESSION_KEY = "agent:session:";

    /**
     * 拆分红包
     */
    public static String SPILT_RED_ENVELOPE = "red:spilt:";

    /**
     * 拆分红包领取
     */
    public static String SPILT_RED_ENVELOPE_GET = "red:spilt:get:";

    /**
     * 红包
     */
    public static String RED_ENVELOPE = "red:";

    /**
     * 红包领取记录
     */
    public static String RED_ENVELOPE_GET_RECORD = "red:get:record:";

    public static String RECOMMEND_PRODUCT = "product:recommend";

    public static String ACCOUNT_TRANSACTION_TYPE = "account:transaction:type";

    public static String COIN_BASE_LIST = "coin:base:list";

    public static String COIN_BASE = "coin:base:";

    public static String COIN_PUSH_LIST = "coin:push:list";

    public static final String USER_WITHDRAW_LIMIT = "user:withdraw:limit:";
}
