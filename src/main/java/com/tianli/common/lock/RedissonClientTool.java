package com.tianli.common.lock;

import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Component

public class RedissonClientTool {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private WebHookService webHookService;

    public void tryLock(String key, VoidHandler handler, ErrorCodeEnum errorCodeEnum) {
        tryLock(key, handler, errorCodeEnum, 3L, TimeUnit.SECONDS);
    }

    public <T> T tryLock(String key, ReturnHandler<T> handler, ErrorCodeEnum errorCodeEnum) {
        return tryLock(key, handler, errorCodeEnum, 3L, TimeUnit.SECONDS);
    }

    public <T> T tryLock(String key, ReturnHandler<T> handler, ErrorCodeEnum errorCodeEnum, long time, TimeUnit unit) {
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean lock = rLock.tryLock(time, unit);
            if (lock) {
                return handler.execute();
            }
            webHookService.dingTalkSend("获取锁超时:" + key);
            throw errorCodeEnum.generalException();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw errorCodeEnum.generalException();
        } finally {
            rLock.unlock();
        }
    }

    public void tryLock(String key, VoidHandler handler, ErrorCodeEnum errorCodeEnum, long time, TimeUnit unit) {
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean lock = rLock.tryLock(time, unit);
            if (lock) {
                handler.execute();
            }
            webHookService.dingTalkSend("获取锁超时:" + key);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw errorCodeEnum.generalException();
        } finally {
            rLock.unlock();
        }
    }
}
