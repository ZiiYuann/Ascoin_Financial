package com.tianli.sso.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonSyntaxException;
import com.tianli.common.Constants;
import com.tianli.exception.ErrCodeException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
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
        return ossServiceVerify(cookie, tokenType, "", "");
    }

    public Result ossServiceVerify(String cookie, LoginTokenType tokenType, String apiPath, String api_method) {
        if (StringUtils.isBlank(cookie)) return null;
        // wallet_news校验管理员登录状态
        String walletNewsServerUrl = configService.getOrDefault(WALLET_NEWS_SERVER_URL, "https://wallet-news.giantdt.com");
        String walletNewsOssVerifyPath = configService.getOrDefault(WALLET_NEWS_OSS_VERIFY_PATH, "/api/sso/verify");
        String response = HttpUtil.post(walletNewsServerUrl + walletNewsOssVerifyPath, JSONUtil.toJsonStr(MapTool.Map()
                .put("token", cookie)
                .put("type", tokenType)
                .put("api", apiPath)
                .put("api_method", api_method)
                .put("trace_id", requestInitService.requestId())
        ));
        Result result = null;
        try {
            result = Constants.GSON.fromJson(response, Result.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            throw ErrorCodeEnum.SSO_SERVICE_ERROR.generalException();
        }
        return result;
    }

    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
}
