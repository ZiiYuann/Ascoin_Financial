package com.tianli.config.aspect;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.RequestRiskManagementInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.user.UserService;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 *   用户风控日志记录AOP
 * </P>
 */

@Aspect
@Component
@Slf4j
public class GoogleRecaptchaCheckAspect {

    @Resource
    RequestInitService requestInitService;

    @Resource
    UserIpLogService userIpLogService;

    @Resource
    UserService userService;

    @Resource
    UserInfoService userInfoService;

    /**
     * 谷歌人机校验
     *
     * @param grcPrivilege 注解
     */
    @Before(value = "@annotation(grcPrivilege)")
    public void judgePrivilege(JoinPoint joinPoint, GrcPrivilege grcPrivilege) throws Throwable {
        long millis = System.currentTimeMillis();
        // 执行过慢, 改为异步任务解析更新
//        Map<String, String> stringStringMap = IPUtils.ipAnalysis(requestInitService.ip());
        System.out.println("执行完ip解析查询: " + (System.currentTimeMillis() - millis) + "ms");
        RequestRiskManagementInfo requestInitServiceRisk = requestInitService.getRisk();
        {
            Signature signature = joinPoint.getSignature();
            MethodSignature ms = (MethodSignature)signature;
            requestInitServiceRisk.setRooMethodName(ms.getDeclaringType().getName() + "#" + ms.getName());
        }
        for (GrcCheckModular grcCheckModular : grcPrivilege.mode()) {
            Object[] args = joinPoint.getArgs();
            if((Objects.equals(grcCheckModular, GrcCheckModular.验证码登录)
                    || Objects.equals(grcCheckModular, GrcCheckModular.密码登录))
                    && StringUtils.isBlank(requestInitServiceRisk.getUsername())){
                Gson gson = new Gson();
                for(Object o : args){
                    JsonObject jsonObject = gson.fromJson(gson.toJson(o), JsonObject.class);
                    if(Objects.nonNull(jsonObject)){
                        JsonElement usernameElement = jsonObject.get("username");
                        if(Objects.nonNull(usernameElement)){
                            String username = usernameElement.getAsString();
                            requestInitServiceRisk.setUsername(username);
                        }
                    }
                }
            }

            if(StringUtils.isBlank(requestInitServiceRisk.getUsername())){
                Long uid = requestInitService._uid();
                if(Objects.nonNull(uid)){
                    // 带有token
                    UserInfo userInfo = userInfoService.getOrSaveById(uid);
                    if (Objects.nonNull(userInfo)){
                        requestInitServiceRisk.setUsername(userInfo.getUsername());
                        requestInitServiceRisk.setNick(userInfo.getNick());
                    }else{
                        ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
                    }
                }else{
                    // 为带有token
                    ErrorCodeEnum.UNLOIGN.throwException();
                }
            }
            userIpLogService.addWithNewT(grcCheckModular);
        }
        System.out.println(new Gson().toJson(requestInitServiceRisk));
//        requestInitService.grcOk();
        System.out.println("前置处理器执行完: " + (System.currentTimeMillis() - millis) + "ms");
    }

}
