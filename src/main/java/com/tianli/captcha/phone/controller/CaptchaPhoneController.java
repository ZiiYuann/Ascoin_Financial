package com.tianli.captcha.phone.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.captcha.phone.controller.dto.PhoneCodeDTO;
import com.tianli.captcha.phone.controller.dto.PhoneCodeVerifyDTO;
import com.tianli.captcha.phone.mapper.CaptchaPhoneMapper;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.mapper.SmsChannel;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 5:31 PM
 */
@RestController
@RequestMapping("/captcha/phone")
public class CaptchaPhoneController {
    @PostMapping("/code")
    public Result code(@RequestBody @Valid PhoneCodeDTO phoneCodeDTO) {
        if (captchaPhoneMapper.countByNum(phoneCodeDTO.getRegionNo()) <= 0) {
            ErrorCodeEnum.MSG_NOT_SUPPORT.throwException();
        }
        String phone = phoneCodeDTO.getPhone();
        CaptchaPhoneType type = phoneCodeDTO.getType();
        if (type == CaptchaPhoneType.resetPayPassword || type == CaptchaPhoneType.resetLoginPassword) {
            User user = userService._getByUsername(phone);
            if (Objects.isNull(user)) {
                ErrorCodeEnum.USER_NOT_EXIST.throwException();
            }
            if (user.getStatus() == UserStatus.disable) {
                ErrorCodeEnum.ACCOUNT_BAND.throwException();
            }
        }
        if (ObjectUtil.equal(type, CaptchaPhoneType.kyc)) {
            userService._my();
        }
        String regionNo = phoneCodeDTO.getRegionNo();
        String code = captchaPhoneService.code(phone, type, regionNo);
        if (Objects.equals(configService.getOrDefaultNoCache("captcha_phone_switch", "false"), "false")) {
            code = "";
        }
        return Result.success(code);
    }

    @PostMapping("/verify")
    public Result verify(@RequestBody @Valid PhoneCodeVerifyDTO phoneCodeVerifyDTO) {
        captchaPhoneService.verify(phoneCodeVerifyDTO.getPhone(), phoneCodeVerifyDTO.getType(), phoneCodeVerifyDTO.getCode());
        return Result.instance();
    }

    @GetMapping("/smsChannel")
    public Result smsChannel() {
        List<SmsChannel> smsChannels = captchaPhoneMapper.selectAll();
        return Result.success(smsChannels);
    }


    @Resource
    CaptchaPhoneMapper captchaPhoneMapper;

    @Resource
    private CaptchaPhoneService captchaPhoneService;

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;
}
