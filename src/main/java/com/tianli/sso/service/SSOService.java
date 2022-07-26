package com.tianli.sso.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.common.ConfigConstants;
import com.tianli.common.Constants;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.permission.LoginTokenType;
import com.tianli.tool.MapTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SSOService {

    public static final String WALLET_NEWS_SERVER_URL = "wallet_news_server_url";

    public static final String WALLET_NEWS_OSS_VERIFY_PATH = "wallet_news_oss_verify_path";

    public Result ossServiceVerify(String cookie, LoginTokenType tokenType) {
        if (StringUtils.isBlank(cookie)) return null;
        // wallet_news校验管理员登录状态
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL, "http://wallet-news.abctest.pro");
        String walletNewsOssVerifyPath = configService.getOrDefault(WALLET_NEWS_OSS_VERIFY_PATH, "/api/sso/verify");
        String result = HttpUtil.post(walletNewsServerUrl + walletNewsOssVerifyPath, JSONUtil.toJsonStr(MapTool.Map()
                .put("token", cookie)
                .put("type", tokenType)));
        return Constants.GSON.fromJson(result, Result.class);
    }

    @Resource
    private ConfigService configService;
}