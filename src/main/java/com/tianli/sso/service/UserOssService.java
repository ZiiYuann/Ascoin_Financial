package com.tianli.sso.service;

import com.google.gson.JsonObject;
import com.tianli.common.Constants;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.permission.LoginTokenType;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
@Service
public class UserOssService {

    /**
     * {
     *     "id":用户id,
     *     "address":地址(可能是助记词的eth地址/私钥的对应链的地址)
     * }
     */
    public JsonObject loginUser() {
        String token = httpServletRequest.getHeader("token");
        if (StringUtils.isNotBlank(token) && token.length() < 256) {
            return ossServiceVerify(token);
        }
        return null;
    }

    private JsonObject ossServiceVerify(String token) {
        Result res = ossService.ossServiceVerify(token, LoginTokenType.USER);
        Object data;
        if (Objects.isNull(res)
                || !StringUtils.equals(res.getCode(), "0")
                || Objects.isNull(data = res.getData()) ) {
            return null;
        }
        return Constants.GSON.fromJson(data.toString(), JsonObject.class);
    }

    private static final String COOKIE_NAME = "_r";
    private static final String SESSION_TMP = "_r";

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private OssService ossService;
}
