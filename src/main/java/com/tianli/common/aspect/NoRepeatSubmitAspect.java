package com.tianli.common.aspect;

import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Slf4j
@Aspect
@Component
@Order(99)
public class NoRepeatSubmitAspect {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RequestInitService requestInitService;


    @Pointcut("@annotation(com.tianli.common.annotation.NoRepeatCommit)")
    public void point() {
    }


    @Around("point()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Long uid = requestInitService.uid();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        NoRepeatCommit noRepeatCommit = method.getAnnotation(NoRepeatCommit.class);

        int expire = noRepeatCommit.expire();

        if (expire < 0) {
            expire = 3;
        }

        String key = StringUtils.isBlank(noRepeatCommit.value()) ? method.getName() : noRepeatCommit.value();

        // 获取锁
        RLock lock = redissonClient.getLock(uid + ":" + key);
        boolean isSuccess = lock.tryLock(-1, expire ,TimeUnit.SECONDS);
        // 获取成功
        if (isSuccess) {
            // 执行请求
            var result = pjp.proceed();
            // 释放锁，3s后让锁自动释放，也可以手动释放
            if(noRepeatCommit.autoUnlock()){
                lock.unlock();
            }
            return result;
        } else {
            // 失败，认为是重复提交的请求
            throw  ErrorCodeEnum.TOO_FREQUENT.generalException();
        }
    }


}
