package com.tianli.common;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 6:17 PM
 */
@Service
public class RedisService {

    public boolean expireLock(String key, long time) {
        return expireLock(key, time, TimeUnit.MILLISECONDS);
    }

    public boolean expireLock(String key, long time, TimeUnit timeUnit) {
        Boolean absent = redisTemplate.boundValueOps(key).setIfAbsent(1L, time, timeUnit);
        return absent != null && absent;
    }


    public boolean timesLock(String key, int times, long expire_time) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        Long increment = ops.increment();
        ops.expire(expire_time, TimeUnit.MILLISECONDS);
        return increment == null || increment < times;
    }


    public void determineTimesLock(String key, int times, long expire_time) {
        redisTemplate.boundValueOps(key).set(times, expire_time, TimeUnit.MILLISECONDS);
    }

    public boolean consumeDetermineTimesLock(String key) {
        Long decrement = redisTemplate.boundValueOps(key).decrement();
        if (decrement == null || decrement <= 0L) {
            redisTemplate.delete(key);
            return false;
        } else
            return true;
    }


    public boolean exists(final String key) {
        Boolean aBoolean = redisTemplate.hasKey(key);
        return aBoolean != null && aBoolean;
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(key, value, expireTime, TimeUnit.SECONDS);
        return true;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(key, value, expireTime, timeUnit);
        return true;
    }

    @Resource
    RedisTemplate<String, Object> redisTemplate;
}
