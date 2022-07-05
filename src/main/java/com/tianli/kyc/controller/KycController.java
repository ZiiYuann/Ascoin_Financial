package com.tianli.kyc.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.captcha.phone.mapper.CaptchaPhoneMapper;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kyc.KycService;
import com.tianli.kyc.mapper.Kyc;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/kyc")
public class KycController {

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private KycService kycService;

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    CaptchaPhoneMapper captchaPhoneMapper;

    @Resource
    CaptchaPhoneService captchaPhoneService;

    @GetMapping("/info")
    public Result kycInfo() {
        Long uid = requestInitService.uid();
        Kyc kyc = kycService.getOne(Wrappers.lambdaQuery(Kyc.class)
                .eq(Kyc::getUid, uid)
                .orderByDesc(Kyc::getId)
                .last("LIMIT  1"));
        if (Objects.isNull(kyc)) {
            return Result.success();
        }
        return Result.success(KycInfoVO.convert(kyc));
    }

    @PostMapping("/upload")
    public Result uploadKyc(@RequestBody @Valid KycUploadCmd cmd) {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (Objects.isNull(user)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Kyc kyc = kycService.getOne(Wrappers.lambdaQuery(Kyc.class)
                .eq(Kyc::getUid, uid)
                .orderByDesc(Kyc::getId)
                .last("LIMIT  1"));
        if (Objects.nonNull(kyc)) {
            ErrorCodeEnum.throwException("已有kyc认证");
        }
        boolean phone_check = isPhone_check(cmd.getCountry(), cmd.getPhone_code(), cmd.getPhone());
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        LocalDateTime now = LocalDateTime.now();
        kycService.save(Kyc.builder()
                .id(CommonFunction.generalId())
                .create_time(now)
                .update_time(now)
                .uid(uid)
                .username(user.getUsername())
                .nick(userInfo.getNick())
                .real_name(cmd.getReal_name())
                .country(cmd.getCountry())
                .certificate_type(cmd.getCertificate_type())
                .certificate_no(cmd.getCertificate_no())
                .status(0)
                .front_image(cmd.getFront_image())
                .behind_image(cmd.getBehind_image())
                .hold_image(cmd.getHold_image())
                .phone(cmd.getPhone())
                .phone_check(phone_check)
                .build());
        return Result.success();
    }

    private boolean isPhone_check(String country, String phone_code, String phone) {
        Integer count = captchaPhoneMapper.countByNum(country);
        if (count > 0 && StrUtil.isBlank(phone_code)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        boolean phone_check = false;
        if (StrUtil.isNotBlank(phone_code)) {
            captchaPhoneService.verify(phone, CaptchaPhoneType.kyc, phone_code);
            phone_check = true;
        }
        return phone_check;
    }

    @PostMapping("/update")
    public Result updateKyc(@RequestBody @Valid KycUpdateCmd cmd) {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (Objects.isNull(user)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Kyc dbKyc = kycService.getById(cmd.getId());
        if (Objects.isNull(dbKyc) || !Objects.equals(dbKyc.getUid(), uid)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        boolean phone_check;
        if (StrUtil.equals(cmd.getPhone(), dbKyc.getPhone()) && StrUtil.isNotBlank(cmd.getPhone_code())) {
            phone_check = isPhone_check(cmd.getCountry(), cmd.getPhone_code(), cmd.getPhone());
        } else if (!StrUtil.equals(cmd.getPhone(), dbKyc.getPhone())) {
            phone_check = isPhone_check(cmd.getCountry(), cmd.getPhone_code(), cmd.getPhone());
        } else {
            phone_check = dbKyc.getPhone_check();
        }
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        kycService.update(Wrappers.lambdaUpdate(Kyc.class)
                .eq(Kyc::getId, cmd.getId())
                .set(Kyc::getUpdate_time, LocalDateTime.now())
                .set(Kyc::getNick, userInfo.getNick())
                .set(Kyc::getReal_name, cmd.getReal_name())
                .set(Kyc::getCountry, cmd.getCountry())
                .set(Kyc::getCertificate_type, cmd.getCertificate_type())
                .set(Kyc::getCertificate_no, cmd.getCertificate_no())
                .set(Kyc::getStatus, 0)
                .set(Kyc::getFront_image, cmd.getFront_image())
                .set(Kyc::getBehind_image, cmd.getBehind_image())
                .set(Kyc::getPhone, cmd.getPhone())
                .set(Kyc::getPhone_check, phone_check)
                .set(Kyc::getHold_image, cmd.getHold_image()));
        return Result.success();
    }

}

