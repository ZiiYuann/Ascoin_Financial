package com.tianli.task;

import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Component
public class RedEnvelopeTask {

    @Resource
    private RedissonClientTool redissonClientTool;
    @Resource
    private RedEnvelopeService redEnvelopeService;

//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void redEnvelopeExpiration() {
        LocalDateTime now = LocalDateTime.now();
        String nowString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        redissonClientTool.tryLock(RedisLockConstants.RED_ENVELOPE_EXPIRATION + nowString
                , () -> redEnvelopeService.redEnvelopeExpiration(now), ErrorCodeEnum.SYSTEM_ERROR, false);
    }
}
