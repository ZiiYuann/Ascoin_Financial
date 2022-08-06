package com.tianli.config.aspect;

import cn.hutool.core.date.DateUtil;
import com.tianli.common.annotation.NoOperation;
import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class NoOperationAspect {

    @Pointcut("@annotation(noOperation)")
    public void pointCut(NoOperation noOperation) {
    }

    @Before("pointCut(noOperation)")
    public void before(JoinPoint jp,NoOperation noOperation) {
        Date now = new Date();
        int lockTime = noOperation.lockTime();
        Date beginTime = DateUtil.offsetMinute(DateUtil.beginOfHour(now),lockTime);
        Date endTime = DateUtil.offsetMinute(DateUtil.endOfHour(now),-lockTime);
        if(!DateUtil.isIn(now,beginTime,endTime)){
            ErrorCodeEnum.NO_OPERATION.throwException();
        }

    }
}
