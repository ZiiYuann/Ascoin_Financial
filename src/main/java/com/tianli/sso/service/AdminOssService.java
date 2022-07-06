package com.tianli.sso.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.common.Constants;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.CookieTool;
import com.tianli.tool.MapTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Service
public class AdminOssService {

    public void loginStatus() {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        if(StringUtils.isBlank(cookie) || cookie.length() > 256){
            // cookie不存时获取请求头中信息
            Object headerSession = httpServletRequest.getHeader(SESSION_TMP);
            if(Objects.isNull(headerSession)){
                ErrorCodeEnum.UNLOIGN.throwException();
            }
            String sessionInfo = headerSession.toString();
            String userIdStr = sessionInfo.split("-")[0];
            cookie = sessionInfo.replaceAll(userIdStr + "-","");
        }
        this.ossServiceVerify(cookie);
    }

    private void ossServiceVerify(String cookie) {
        if (StringUtils.isBlank(cookie)) ErrorCodeEnum.UNLOIGN.throwException();
        // wallet_news校验管理员登录状态
        String walletNewsServerUrl = configService.getOrDefault(ConfigConstants.WALLET_NEWS_SERVER_URL, "http://wallet-news.abctest.pro");
        String walletNewsOssVerifyPath = configService.getOrDefault(ConfigConstants.WALLET_NEWS_OSS_VERIFY_PATH, "/api/oss/verify");
        String result = HttpUtil.post(walletNewsServerUrl + walletNewsOssVerifyPath, JSONUtil.toJsonStr(MapTool.Map()
                .put("token", cookie)
                .put("tokenType", "ADMIN")));
        Result res = Constants.GSON.fromJson(result, Result.class);
        Object data = null;
        if (Objects.isNull(res)
                || !StringUtils.equals(res.getCode(), "0")
                || Objects.isNull(data = res.getData()) ) {
            ErrorCodeEnum.UNLOIGN.throwException();
        }
        // todo: data转成adminInfo
        AdminInfo adminInfo = AdminContent.get();

    }

    private static final String COOKIE_NAME = "_r";
    private static final String SESSION_TMP = "_r";

    @Resource
    private ConfigService configService;
    @Resource
    private HttpServletRequest httpServletRequest;
}
