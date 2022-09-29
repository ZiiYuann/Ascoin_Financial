package com.tianli.common.webhook;

public enum WebHookToken {
    // 公司内部通知群
    FINANCIAL_PRODUCT("fd0634169bdee72d7ce4ab8e9f3464933abed9f1b3ff0d89769d6cd64c0e1e7d"
            , "SEC4f2b3d7b20432f7aa3a6cf9144a15be0e17e38919d576dfac9ca2fa6b4b877f5"),

    // 基金正式通知
    FUND_PRODUCT("e9ba9212c2bfebc948d844dc7d5ba72a82acf79be36cbc46d5e507a8fa13c",
            "SEC46e08bbc82c43cfa0a35aba643eef004cfe78ac774790eba341a21b7079b6d03"),

    // 异常通知 && 测试通知
    BUG_PUSH("1a1216a39f18e8022b6795014424a9fcf5d62a5f00d3666c11127b21841eb718"
            ,"SEC52152f460aaf1c4c77592f46674aadf9592fcca6d99974b0b7fb74cd66f20be3")
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
