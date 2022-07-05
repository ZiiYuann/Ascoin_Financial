package com.tianli.user.password.controller;

import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import com.tianli.user.password.UserPasswordService;
import com.tianli.user.password.mapper.UserPassword;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2020/3/12 16:05
 */

@RestController
@RequestMapping("/user/password")
public class UserPasswordController {
    @PostMapping("/loginPassword")
    public Result loginPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        User user = userService._getByUsername(userPasswordDTO.getPhone());
        if (user == null) ErrorCodeEnum.USER_NOT_EXIST.throwException();
//        Long uid = requestInitService.uid();
//        if (!Objects.equals(uid, user.getId())) ErrorCodeEnum.ACCESS_DENY.throwException();
        if (Objects.equals(user.getStatus(), UserStatus.disable)) ErrorCodeEnum.ACCOUNT_BAND.throwException();
//        captchaPhoneService.verify(userPasswordDTO.getPhone(), CaptchaPhoneType.resetLoginPassword, userPasswordDTO.getCode());
        captchaEmailService.verify(userPasswordDTO.getPhone(), CaptchaPhoneType.resetLoginPassword, userPasswordDTO.getCode());
        userPasswordService.updateLogin(user.getId(), userPasswordDTO.getPassword());
        return Result.instance();
    }

    @PostMapping("/payPassword")
    public Result payPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        Long uid = requestInitService._uid();
        if (uid != null) {
            UserPassword userPassword = userPasswordService.get(uid);
            if (StringUtils.isEmpty(userPassword.getPay_password())) {
                userPasswordService.updatePay(uid, userPasswordDTO.getPassword());
                return Result.instance();
            }
        }
        if (StringUtils.isEmpty(userPasswordDTO.getPhone()) || StringUtils.isEmpty(userPasswordDTO.getCode()))
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        User user = userService._getByUsername(userPasswordDTO.getPhone());
        if (user == null) ErrorCodeEnum.USER_NOT_EXIST.throwException();
        if (!Objects.equals(uid, user.getId())) ErrorCodeEnum.ACCESS_DENY.throwException();
//        captchaPhoneService.verify(userPasswordDTO.getPhone(), CaptchaPhoneType.resetPayPassword, userPasswordDTO.getCode());
        captchaEmailService.verify(userPasswordDTO.getPhone(), CaptchaPhoneType.resetPayPassword, userPasswordDTO.getCode());
        userPasswordService.updatePay(user.getId(), userPasswordDTO.getPassword());
        return Result.instance();
    }

    @PostMapping("/checkOriginalPassword/{type}")
    public Result checkOriginalPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO, @PathVariable("type") String type) {
        Long uid = requestInitService.uid();
        UserPassword userPassword = userPasswordService.get(uid);
        if (Objects.equals(type, "loginPassword")) {
            if (!userPasswordService._checkLoginPassword(userPassword, userPasswordDTO.getPassword())) {
                ErrorCodeEnum.ORIGINAL_PASSWORD_ERROR.throwException();
            }
        } else if (Objects.equals(type, "payPassword")) {
            if (!userPasswordService._checkPayPassword(userPassword, userPasswordDTO.getPassword())) {
                ErrorCodeEnum.ORIGINAL_PASSWORD_ERROR.throwException();
            }
        }else {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwExtendMsgException("类型错误");
        }
        return Result.instance();
    }

    @Resource
    private CaptchaPhoneService captchaPhoneService;
    @Resource
    private CaptchaEmailService captchaEmailService;
    @Resource
    private UserService userService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserPasswordService userPasswordService;
}
