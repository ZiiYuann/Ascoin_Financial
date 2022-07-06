package com.tianli.tool;


import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * ws://financial.abctest.pro/ws?token=abc
 */
public class WebSocketUtils {

    private static final String PREFIX = "ws_push.";

    private static final String PUSH_ALL = "ws_push.all";

    public static void convertAndSend(String token, Object msg){
        WebSocketRedis.getRedisTemplate().convertAndSend(PREFIX + Crypto.digestToString(DigestFactory.createMD5(), token), msg);
    }

    public static void convertAndSendAll(Object msg){
        WebSocketRedis.getRedisTemplate().convertAndSend(PUSH_ALL, msg);
    }

    public static void convertAndSendAdmin(Object msg){
        WebSocketRedis.getRedisTemplate().convertAndSend(PREFIX+Crypto.digestToString(DigestFactory.createMD5(), "admin"), msg);
    }

    static class WebSocketRedis {
        private static RedisTemplate<String, Object> REDIS_TEMPLATE = ApplicationContextTool.getBean("redisTemplate", RedisTemplate.class);
        public static RedisTemplate<String, Object> getRedisTemplate(){
            return REDIS_TEMPLATE;
        }
    }

}
