package com.tianli.common.lock;

import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void lock(String key, Long expireTime, TimeUnit timeUnit) {
        if (!this._lock(key, expireTime, timeUnit)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

    public boolean _lock(String key, Long expireTime, TimeUnit timeUnit) {
        String uuid = UUID.randomUUID().toString();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(key);
        Boolean absent = ops.setIfAbsent(uuid, expireTime, timeUnit);
        if (absent != null && absent) {
            Map<String, String> map = threadLocal.get();
            if (map == null) {
                map = new HashMap<>();
                threadLocal.set(map);
            }
            map.put(key, uuid);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 增加锁的时间 (非原子性操作, 高并发情况下谨慎使用)
     * @param expireTime 需要重置设置的时间
     * @param timeUnit 单位
     * @param remainingTime 剩余的时长(s)
     */
    public void _addExpireTime(String key, Long expireTime, TimeUnit timeUnit, long remainingTime) {
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(key);
        Long expire = ops.getExpire();
        if(Objects.isNull(expire) || expire > remainingTime){
            return;
        }
        String val = ops.get();
        if (val != null) {
            Map<String, String> map = threadLocal.get();
            if (map != null) {
                ops.setIfPresent(val, expireTime, timeUnit);
            }
        }
    }

    public void unlock(String key) {
        String attribute = null;
        Map<String, String> map = threadLocal.get();
        if (map != null) {
            attribute = map.get(key);
        }
        if (attribute != null && !attribute.isEmpty()) {
            BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(key);
            String value = ops.get();
            if (!StringUtils.isEmpty(value) && value.equals(attribute)) {
                stringRedisTemplate.delete(key);
                map.remove(key);
            }
        }
    }


    public void unlock() {
        Map<String, String> map = threadLocal.get();
        if (map != null) {
            Set<String> stringSet = new HashSet<>(map.keySet());
            for (String key : stringSet) {
                unlock(key);
            }
        }
    }

    private final ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();
    private final String REDIS_LOCK = "redis_lock_";
}
