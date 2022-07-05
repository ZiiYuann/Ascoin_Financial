package com.tianli.captcha.email.controller;

import com.tianli.captcha.email.controller.dto.EmailCodeDTO;
import com.tianli.captcha.email.controller.dto.PhoneCodeVerifyDTO;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 5:31 PM
 */
@RestController
@RequestMapping("/captcha/email")
public class CaptchaEmailController {
    @PostMapping("/code")
    public Result code(@RequestBody @Valid EmailCodeDTO dto) {
        String email = dto.getEmail();
        CaptchaPhoneType type = dto.getType();
        if(type == CaptchaPhoneType.resetPayPassword || type == CaptchaPhoneType.resetLoginPassword){
            User user = userService._getByUsername(email);
            if(Objects.isNull(user)){
                ErrorCodeEnum.USER_NOT_EXIST.throwException();
            }
            if(user.getStatus() == UserStatus.disable){
                ErrorCodeEnum.ACCOUNT_BAND.throwException();
            }
        }
        String code = captchaEmailService.code(email, type);
        // 验证码是否暴露给前端
        if(Objects.equals(configService.getOrDefaultNoCache("captcha_phone_switch", "false"), "false")){
            code = "";
        }
        return Result.success(code);
    }

    @PostMapping("/verify")
    public Result verify(@RequestBody @Valid PhoneCodeVerifyDTO phoneCodeVerifyDTO) {
        captchaEmailService.verify(phoneCodeVerifyDTO.getEmail(), phoneCodeVerifyDTO.getType(), phoneCodeVerifyDTO.getCode());
        return Result.instance();
    }


    @Resource
    private CaptchaEmailService captchaEmailService;

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;
}
