package com.tianli.common.lock;

import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLock {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void waitLock(String key,long timeoutMillis){
        if(!this.quietWaitLock(key,timeoutMillis)){
            log.error("redis等待锁释放超时,key:{}",key);
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    public boolean quietWaitLock(String key,long timeoutMillis){
        int time = 0;
        while(time < timeoutMillis){
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if(Boolean.TRUE.equals(hasKey)){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                time = time + 100;
                continue;
            }
            return true;
        }
        return false;
    }

    public void isLock(String key){
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

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
