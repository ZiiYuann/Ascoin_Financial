package com.tianli.sso.service;

import com.google.gson.JsonObject;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.permission.admin.AdminContent;
import com.tianli.sso.permission.admin.AdminInfo;
import com.tianli.sso.permission.LoginTokenType;
import com.tianli.tool.CookieTool;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
@Service
public class AdminOssService {

    public void loginStatus() {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        log.info("AdminOssService-loginStatus-cookie:{}", cookie);
        if(StringUtils.isBlank(cookie) || cookie.length() > 256){
            // cookie不存时获取请求头中信息
            Object headerSession = httpServletRequest.getHeader(SESSION_TMP);
            if(Objects.isNull(headerSession)){
                ErrorCodeEnum.SESSION_TMP_R_NOT_EXIST.throwException();
            }
            String sessionInfo = headerSession.toString();
            //eg: 1687406546639098600-fa6b466a-24b5-4747-9c07-6cc1506aa1e6
            String userIdStr = sessionInfo.split("_")[0];
            if (!StringUtils.equals(userIdStr, sessionInfo)){
                cookie = sessionInfo.replaceAll(userIdStr + "_","");
            }else {
                cookie = sessionInfo;
            }
        }
        this.ossServiceVerify(cookie);
    }

    private void ossServiceVerify(String cookie) {
        AdminInfo adminInfo = AdminContent.get();
        String api = adminInfo.getApi();
        String api_method = adminInfo.getApi_method();
        Result res = ssoService.ossServiceVerify(cookie, LoginTokenType.ADMIN, StringUtils.isBlank(api) ? "" : "/fapi".concat(api), api_method);
        Object data = null;
        if (Objects.isNull(res)
                || !StringUtils.equals(res.getCode(), "0")
                || Objects.isNull(data = res.getData()) ) {
            //     ACCESS_DENY(103, "无权限"),
            if (StringUtils.equals(res.getCode(), "103")) {
                ErrorCodeEnum.ACCESS_DENY.throwException();
            }
            log.error("login error[1]");
            ErrorCodeEnum.UNLOIGN.throwException();
        }
        JsonObject dataJsonObj = Constants.GSON.fromJson(data.toString(), JsonObject.class);
        adminInfo.setAid(JsonObjectTool.getAsLong(dataJsonObj, "uid"))
                .setPhone(JsonObjectTool.getAsString(dataJsonObj, "phone"))
                .setUsername(JsonObjectTool.getAsString(dataJsonObj, "username"))
                .setNickname(JsonObjectTool.getAsString(dataJsonObj, "nickname"));
    }

    private static final String COOKIE_NAME = "_r";
    private static final String SESSION_TMP = "_r";

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private SSOService ssoService;
}
