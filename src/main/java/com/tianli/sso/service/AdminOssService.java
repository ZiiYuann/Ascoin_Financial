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
        Result res = ossService.ossServiceVerify(cookie, LoginTokenType.ADMIN);
        Object data = null;
        if (Objects.isNull(res)
                || !StringUtils.equals(res.getCode(), "0")
                || Objects.isNull(data = res.getData()) ) {
            ErrorCodeEnum.UNLOIGN.throwException();
        }
        JsonObject dataJsonObj = Constants.GSON.fromJson(data.toString(), JsonObject.class);
        AdminInfo adminInfo = AdminContent.get();
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
    private OssService ossService;
}
