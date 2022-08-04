package com.tianli.config.aspect;

import cn.hutool.core.date.DateTime;
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
        Date beginOfHour = DateUtil.beginOfHour(now);
        DateTime endOfHour = DateUtil.endOfHour(now);
        Date beginTime = DateUtil.offsetMinute(beginOfHour,5);
        Date endTime = DateUtil.offsetMinute(endOfHour,-5);
        if(!DateUtil.isIn(now,beginTime,endTime)){
            ErrorCodeEnum.NO_OPERATION.throwException();
        }

    }
}
