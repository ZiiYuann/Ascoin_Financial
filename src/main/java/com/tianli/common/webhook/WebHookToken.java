package com.tianli.common.webhook;

public enum WebHookToken {
    // 公司内部通知群
    FINANCIAL_PRODUCT("fd0634169bdee72d7ce4ab8e9f3464933abed9f1b3ff0d89769d6cd64c0e1e7d"
            , "SEC4f2b3d7b20432f7aa3a6cf9144a15be0e17e38919d576dfac9ca2fa6b4b877f5"),

    // 基金正式通知
    FUND_PRODUCT("e2462fc274c10ebd0cdc8e3c3e68da89da9cc98fb839ce000c50e96b233bc7e5",
            "SEC8063b75ee9ef2a2ca0bc8df4a727aeca30a30eeddaef0b8635acbc3585d4b925"),

    // 异常通知 && 测试通知
    BUG_PUSH("1a1216a39f18e8022b6795014424a9fcf5d62a5f00d3666c11127b21841eb718"
            ,"SEC52152f460aaf1c4c77592f46674aadf9592fcca6d99974b0b7fb74cd66f20be3"),
    PRO_BUG_PUSH("4b8ddd72fc13cc7a929f9e77b138cd3a78b7898cf2648638ee11cc543b78c525"
            ,"SECd98cf74417522963156c2489b5bf5c61449ccd8ec03bb07a6031d2c856f9c3bc")
    ;

    WebHookToken(String token, String secret) {
        this.token = token;
        this.secret = secret;
    }

    private final String token;
    private final String secret;

    public String getSecret() {
        return secret;
    }

    public String getToken() {
        return token;
    }
}
