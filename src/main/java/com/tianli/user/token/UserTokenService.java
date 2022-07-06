package com.tianli.user.token;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.captcha.email.EmailSendFactory;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.RequestInitToken;
import com.tianli.common.init.RequestRiskManagementInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.GoogleVerify;
import com.tianli.user.UserService;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2019/2/27 10:15 AM
 */

@Service
@Slf4j
public class UserTokenService implements RequestInitToken {

    public String login(User user) {
        if (UserStatus.disable.equals(user.getStatus()))
            ErrorCodeEnum.ACCOUNT_BAND.throwException();
        String token = UUID.randomUUID().toString().replace("-", "");

        UserToken userToken = userTokenMapper.selectOne(new LambdaQueryWrapper<UserToken>().eq(UserToken::getUid, user.getId()).eq(UserToken::getImei, requestInitService.imei()));

        if (userToken == null) {
            userToken = new UserToken();
            userToken.setId(CommonFunction.generalId());
            userToken.setUid(user.getId());
            userToken.setImei(requestInitService.imei());
            userToken.setCreate_time(requestInitService.now());
            userToken.setToken(token);
            userTokenMapper.insert(userToken);
        } else {
            userTokenMapper.update(null, new LambdaUpdateWrapper<UserToken>()
                    .set(UserToken::getToken, token)
                    .set(UserToken::getUpdate_time, LocalDateTime.now())
                    .eq(UserToken::getId, userToken.getId()));
        }

        redisTemplate.boundValueOps(REDIS_PREFIX + token).set(user.getId(), 30L, TimeUnit.MINUTES);
        String ip = requestInitService.ip();
        if (StrUtil.isNotBlank(ip) && !userIpLogService.isCommonIp(user.getId(),ip)) {
            try {
                emailSendFactory.send(EmailSendEnum.IP_WARN,null,ip,user.getUsername());
            } catch (Exception e) {
                log.info("发送ip异常邮件失败:",e);
            }
        }
        return token;
    }

    public Long currentUserId(HttpServletRequest httpServletRequest) {
        Long id = null;
        String token = httpServletRequest.getHeader("token");
        if (!StringUtils.isEmpty(token) && token.length() < 256) {
            Object o = redisTemplate.boundValueOps(REDIS_PREFIX + token).get();
            if (o != null) {
                id = Long.valueOf(o.toString());
            } else {
                UserToken userToken = userTokenMapper.selectOne(new LambdaQueryWrapper<UserToken>().eq(UserToken::getToken, token));
                if (userToken != null) {
                    redisTemplate.boundValueOps(REDIS_PREFIX + token).set(userToken.getUid(), 30L, TimeUnit.MINUTES);
                    id = userToken.getUid();
                }
            }
        }
        if (id != null && !userService.status(id))
            id = null;
        return id;
    }

    @Override
    public void googleCheck(HttpServletRequest httpServletRequest, RequestRiskManagementInfo riskInfo) {
        String googleCheckCode = httpServletRequest.getHeader("g-recaptcha-code");
        if(org.apache.commons.lang3.StringUtils.isBlank(googleCheckCode)){
            return;
        }
        String g_recaptcha_min_score = configService.getOrDefault("g_recaptcha_min_score", "0.5");
        double minScore = Double.parseDouble(g_recaptcha_min_score);
        JsonObject jsonObject = GoogleVerify.check_(googleCheckCode);
        JsonElement success;
        if(Objects.isNull(jsonObject) || Objects.isNull((success = jsonObject.get("success"))) || !success.getAsBoolean()){
            return;
        }
        JsonElement score = jsonObject.get("score");
        boolean res = Objects.nonNull(score) && score.getAsDouble() >= minScore;
        if(Objects.nonNull(score)){
            riskInfo.setGrcScore(score.getAsDouble());
        }
        riskInfo.setGrc(res);
    }

    public static void main(String[] args) {
        System.out.println("sss".substring(0,0));
        System.out.println("sss".indexOf(""));
    }

    private final String REDIS_PREFIX = "UserTokenService_token_";


    //    @Resource
//    private UserDenyService userDenyService;
    @Resource
    EmailSendFactory emailSendFactory;
    @Resource
    UserIpLogService userIpLogService;
    @Resource
    private UserService userService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private UserTokenMapper userTokenMapper;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
}
