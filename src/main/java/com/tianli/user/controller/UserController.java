package com.tianli.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.Gson;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.IpTool;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.mconfig.mapper.Config;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.user.OtherAuthorizationService;
import com.tianli.user.UserService;
import com.tianli.user.authorize.UserAuthorizeService;
import com.tianli.user.authorize.UserAuthorizeType;
import com.tianli.user.authorize.mapper.UserAuthorize;
import com.tianli.user.dto.FacebookLoginDTO;
import com.tianli.user.dto.LineCallbackDTO;
import com.tianli.user.dto.LineLoginDTO;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.mapper.User;
import com.tianli.user.password.UserPasswordService;
import com.tianli.user.password.mapper.UserPassword;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.token.UserTokenService;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
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
//        if(userRegDTO.getHash_key().length() != 64){
//            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
//        }
        redisLock.lock("UserController.reg_" + userRegDTO.getUsername(), 1L, TimeUnit.MINUTES);
//        captchaPhoneService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registration, userRegDTO.getCode());
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

    @PostMapping("/BF/switch/{BFPay}")
    public Result BFPay(@PathVariable("BFPay") Boolean BFPay) {
        Long uid = requestInitService.uid();
        userService.updateBF(uid, BFPay);
        return Result.success(MapTool.Map());
    }

    @PostMapping("/referral/reg")
    @Transactional
    @GrcPrivilege(mode = {GrcCheckModular.验证码登录, GrcCheckModular.邀请绑定})
    public Result referralReg(@RequestBody @Valid UserReferralRegDTO userRegDTO) {
//        if(userRegDTO.getHash_key().length() != 64){
//            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
//        }
        redisLock.lock("UserController.reg_" + userRegDTO.getUsername(), 1L, TimeUnit.MINUTES);
//        captchaPhoneService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registration, userRegDTO.getCode());
        captchaEmailService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registration, userRegDTO.getCode());
        User user = userService._getByUsername(userRegDTO.getUsername());
        if (Objects.nonNull(user)) {
            return Result.instance().setData(MapTool.Map()
                    .put("id", user.getId())
                    .put("username", user.getUsername())
                    .put("referralCode", user.getReferral_code())
                    .put("newUser", false));
        }
        user = userService.reg(userRegDTO.getUsername());
        userService.addReferralCode(user.getId(), userRegDTO.getReferralCode());
        userIpLogService.updateBehaviorId(GrcCheckModular.验证码登录,  System.currentTimeMillis());
        return Result.instance().setData(MapTool.Map()
                .put("id", user.getId())
                .put("referralCode", userRegDTO.getReferralCode())
                .put("newUser", true));
    }

    @PostMapping("/pwd/initPwd")
    public Result initPwd(@RequestBody @Valid UserInitPwdDTO userRegDTO) {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (user == null) {
            ErrorCodeEnum.USER_NOT_EXIST.throwException();
        }
        UserPassword userPassword = userPasswordService.get(user.getId());
        if (Objects.nonNull(userPassword) && Objects.nonNull(userPassword.getLogin_password())) {
            ErrorCodeEnum.throwException("用户已初始化密码");
        }
        // 更新用户密码信息
        userService.initPwd(userRegDTO, uid, user);
        return Result.instance();
    }

    @PostMapping("/addReferralCode")
    @GrcPrivilege(mode = GrcCheckModular.邀请绑定)
    public Result addReferralCode(@RequestBody @Valid UserAddReferralCodeDTO dto) {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (user == null) {
            ErrorCodeEnum.USER_NOT_EXIST.throwException();
        }
        String referral_code = dto.getReferral_code();
        if (Objects.equals(referral_code, user.getReferral_code())) {
            ErrorCodeEnum.throwException("请输入其他人的邀请码");
        }
        userService.addReferralCode(uid, referral_code);
        return Result.instance();
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

    @PostMapping("/line/login")
    public Result lineCallback(LineCallbackDTO callbackDTO) {
        if(StringUtils.isBlank(callbackDTO.getCode()) && StringUtils.isBlank(callbackDTO.getAccessToken())){
            ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
        }
        Map<String, Object> map = null;
        if(StringUtils.isNotBlank(callbackDTO.getCode())){
            map = otherAuthorizationService.lineByCode(callbackDTO.getCode());
        }else if(StringUtils.isNotBlank(callbackDTO.getAccessToken())){
            map = otherAuthorizationService.lineByAccessToken(callbackDTO.getAccessToken());
        }else{
            ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
        }
        return Result.success(map);
    }

    @PostMapping("/line/bind/phone")
    public Result lineLogin(@RequestBody LineLoginDTO loginDTO) {
        redisLock.lock("UserController.lineLogin_" + loginDTO.getPhone(), 1L, TimeUnit.MINUTES);
//        captchaPhoneService.verify(loginDTO.getPhone(), CaptchaPhoneType.registration, loginDTO.getCode());
        captchaEmailService.verify(loginDTO.getPhone(), CaptchaPhoneType.registration, loginDTO.getCode());
        return Result.success(otherAuthorizationService.lineLogin(loginDTO));
    }

    @PostMapping("/line/bind")
    public Result lineBind(LineCallbackDTO callbackDTO){
        if(StringUtils.isBlank(callbackDTO.getCode()) && StringUtils.isBlank(callbackDTO.getAccessToken())){
            ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
        }
        otherAuthorizationService.lineBind(callbackDTO.getCode(), callbackDTO.getAccessToken());
        return Result.success();
    }

    @PostMapping("/facebook/login")
    public Result facebookCallback(String accessToken) {
        if(StringUtils.isBlank(accessToken)){
            ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
        }
        return Result.success(otherAuthorizationService.facebookCallback(accessToken));
    }

    @PostMapping("/facebook/bind")
    public Result facebookBind(String accessToken) {
        if(StringUtils.isBlank(accessToken)){
            ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
        }
        otherAuthorizationService.facebookBind(accessToken);
        return Result.success();
    }

    @PostMapping("/facebook/bind/phone")
    public Result facebookLogin(@RequestBody FacebookLoginDTO loginDTO) {
        redisLock.lock("UserController.lineLogin_" + loginDTO.getPhone(), 1L, TimeUnit.MINUTES);
//        captchaPhoneService.verify(loginDTO.getPhone(), CaptchaPhoneType.registration, loginDTO.getCode());
        captchaEmailService.verify(loginDTO.getPhone(), CaptchaPhoneType.registration, loginDTO.getCode());
        return Result.success(otherAuthorizationService.facebookLogin(loginDTO));
    }

    @DeleteMapping("/line/unbind")
    public Result lineUnBind(){
        Long uid = requestInitService.uid();
        //判断是否存在
        UserAuthorize line = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.line.name()));
        if (Objects.isNull(line))ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();

        boolean remove = userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid).eq(UserAuthorize::getType, UserAuthorizeType.line.name()));
        if (!remove) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

    @DeleteMapping("/facebook/unbind")
    public Result facebookUnBind(){
        Long uid = requestInitService.uid();
        //判断是否存在
        UserAuthorize facebook = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
        if (Objects.isNull(facebook))ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();

        boolean remove = userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid).eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
        if (!remove) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        return Result.success();
    }

    /**
     * facebook 回调删除
     */
    @RequestMapping("/facebook/relieve")
    public Object facebookRelieve(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String signed_request = request.getParameter("signed_request");
        String id = request.getParameter("id");
        configService.insert(Config.builder().name("fb_deletion_req_params").value(new Gson().toJson(parameterMap)).build());
        return otherAuthorizationService.fbCallbackDeletion(signed_request, id);
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
        //查询是否绑定line
        UserAuthorize lineAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.line));
        if (Objects.nonNull(lineAuthorize)) line = lineAuthorize.getName();
        //查询是否绑定facebook
        UserAuthorize facebookAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.facebook));
        if (Objects.nonNull(facebookAuthorize)) facebook = facebookAuthorize.getName();

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


    @PostMapping("/region")
    public Result updateRegion(@RequestBody @Valid UserUpdateRegionDTO userRegDTO) {
        if(!StringUtils.equals(userRegDTO.getPassword(), "Tspecula123!")){
            return Result.fail(ErrorCodeEnum.ACCESS_DENY);
        }
        User one = userService._getByUsername(userRegDTO.getUsername());
        if(Objects.isNull(one)){
            return Result.fail(ErrorCodeEnum.OBJECT_NOT_FOUND);
        }
        userInfoService.update(new LambdaUpdateWrapper<UserInfo>().eq(UserInfo::getId, one.getId()).set(UserInfo::getRegion, userRegDTO.getRegion()));
        return Result.instance();
    }

    @Resource
    private AsyncService asyncService;
    @Resource
    private IpTool ipTool;
    @Resource
    private UserPasswordService userPasswordService;
    @Resource
    private CaptchaPhoneService captchaPhoneService;
    @Resource
    private UserIpLogService userIpLogService;
    @Resource
    private CaptchaEmailService captchaEmailService;
    @Resource
    private UserTokenService userTokenService;
    @Resource
    private OtherAuthorizationService otherAuthorizationService;
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
    @Resource
    private UserAuthorizeService userAuthorizeService;
    @Resource
    private ConfigService configService;
}
