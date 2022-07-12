package com.tianli.sso.service;

import com.tianli.common.Constants;
import com.tianli.exception.Result;
import com.tianli.sso.init.SignUserInfo;
import com.tianli.sso.permission.LoginTokenType;
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
    public SignUserInfo loginUser() {
        String token = httpServletRequest.getHeader("token");
        if (StringUtils.isNotBlank(token) && token.length() < 256) {
            return ossServiceVerify(token);
        }
        return null;
    }

    private SignUserInfo ossServiceVerify(String token) {
        Result res = ossService.ossServiceVerify(token, LoginTokenType.USER);
        Object data;
        if (Objects.isNull(res)
                || !StringUtils.equals(res.getCode(), "0")
                || Objects.isNull(data = res.getData()) ) {
            return null;
        }
        return Constants.GSON.fromJson(data.toString(), SignUserInfo.class);
    }

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private OssService ossService;
}
