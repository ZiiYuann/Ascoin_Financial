package com.tianli.config.aspect;

import com.tianli.common.RedisLockConstants;
import com.tianli.common.annotation.NoRepeatSubmit;
import com.tianli.common.lock.RedisLock;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Slf4j
public class RepeatSubmitAspect {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RequestInitService requestInitService;

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }


    @Before("pointCut(noRepeatSubmit)")
    public void before(JoinPoint jp, NoRepeatSubmit noRepeatSubmit) {
        ServletRequestAttributes ra= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ra.getRequest();
        long lockTime = noRepeatSubmit.lockTime();
        Long uid = requestInitService.uid();
        String key = request.getServletPath()+uid;
        redisLock.lock(key,lockTime, TimeUnit.SECONDS);
    }

}
