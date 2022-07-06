package com.tianli.user.controller;

import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.common.IpTool;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.mapper.User;
import com.tianli.user.password.UserPasswordService;
import com.tianli.user.password.mapper.UserPassword;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:56
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/reg")
    @GrcPrivilege(mode = GrcCheckModular.验证码登录)
    public Result reg(@RequestBody @Valid UserRegDTO userRegDTO) {
        redisLock.lock("UserController.reg_" + userRegDTO.getUsername(), 1L, TimeUnit.MINUTES);
        captchaEmailService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registration, userRegDTO.getCode());
        User user = userService._getByUsername(userRegDTO.getUsername());
        boolean newUser = false;
        if (user == null) {
            newUser = true;
            user = userService.reg(userRegDTO.getUsername());
        }
        String token = userTokenService.login(user);
        userIpLogService.updateBehaviorId(GrcCheckModular.验证码登录, System.currentTimeMillis());
        return Result.instance().setData(MapTool.Map()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("token", token)
                .put("newUser", newUser));
    }

    @PostMapping("/login")
    @GrcPrivilege(mode = GrcCheckModular.密码登录)
    public Result login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        redisLock.lock("UserController.login_" + userLoginDTO.getUsername(), 1L, TimeUnit.MINUTES);
        User user = userService._getByUsername(userLoginDTO.getUsername());
        if (user == null) ErrorCodeEnum.USER_NOT_EXIST.throwException();
        UserPassword userPassword = userPasswordService.get(user.getId());
        userPasswordService.checkLoginPassword(userPassword, userLoginDTO.getPassword());
        String token = userTokenService.login(user);
        userIpLogService.updateBehaviorId(GrcCheckModular.密码登录, System.currentTimeMillis());
        return Result.instance().setData(MapTool.Map()
                .put("token", token)
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("newUser", false));
    }

    @GetMapping("/my")
    public Result my() {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        String ip = ipTool.getIp();
        asyncService.asyncSuccessRequest(() -> userService.last(uid, ip));
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        UserReferral userReferral = userReferralService.getById(uid);
        String line = null;
        String facebook = null;

        return Result.instance().setData(MapTool.Map()
                .put("id", user.getId().toString())
                .put("credit_score", user.getCredit_score().toString())
                .put("username", user.getUsername())
                .put("referralCode", user.getReferral_code())
                .put("identity", user.getIdentity())
                .put("myReferralCode", user.getReferral_code())
                .put("referralCode", Objects.isNull(userReferral) ? null : userReferral.getReferral())
                .put("avatar", userInfo.getAvatar())
                .put("nick", userInfo.getNick())
                .put("region", userInfo.getRegion())
                .put("Facebook",facebook)
                .put("Line",line));
    }

    @GetMapping("/logout")
    public Result logout(HttpServletRequest httpServletRequest) {
        return Result.instance();
    }

    @Resource
    private AsyncService asyncService;
    @Resource
    private IpTool ipTool;
    @Resource
    private UserPasswordService userPasswordService;
    @Resource
    private UserIpLogService userIpLogService;
    @Resource
    private CaptchaEmailService captchaEmailService;
    @Resource
    private UserTokenService userTokenService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private UserService userService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private UserReferralService userReferralService;
    @Resource
    private RequestInitService requestInitService;
}
