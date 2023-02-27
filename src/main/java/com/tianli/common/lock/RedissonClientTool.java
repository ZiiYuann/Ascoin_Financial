package com.tianli.common.lock;

import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RedissonClientTool {

    @Resource
    private RedissonClient redissonClient;

    public void tryLock(String key, VoidHandler handler, ErrorCodeEnum errorCodeEnum) {
        tryLock(key, handler, errorCodeEnum, 3L, TimeUnit.SECONDS, true);
    }

    public void tryLock(String key, VoidHandler handler, ErrorCodeEnum errorCodeEnum, boolean throwException) {
        tryLock(key, handler, errorCodeEnum, 3L, TimeUnit.SECONDS, throwException);
    }

    public <T> T tryLock(String key, ReturnHandler<T> handler, ErrorCodeEnum errorCodeEnum) {
        return tryLock(key, handler, errorCodeEnum, 3L, TimeUnit.SECONDS, true);
    }

    public <T> T tryLock(String key, ReturnHandler<T> handler, ErrorCodeEnum errorCodeEnum
            , long time, TimeUnit unit, boolean throwException) {
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean lock = rLock.tryLock(time, unit);
            if (lock) {
                return handler.execute();
            }

            if (throwException) {
                log.info("获取锁超时:{}", key);
                throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
            }
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw errorCodeEnum.generalException();
        } catch (Exception e) {
            throw e;
        } finally {
            rLock.unlock();
        }
    }

    public void tryLock(String key, VoidHandler handler, ErrorCodeEnum errorCodeEnum
            , long time, TimeUnit unit, boolean throwException) {
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean lock = rLock.tryLock(time, unit);
            if (lock) {
                handler.execute();
                return;
            }
            if (throwException) {
                log.info("获取锁超时:{}", key);
                throw errorCodeEnum.generalException();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (Exception e) {
            throw e;
        } finally {
            rLock.unlock();
        }
    }
}
