package com.tianli.captcha;

import com.tianli.tool.CookieTool;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2018/10/16 下午4:06
 */
@Service
public class CaptchaService {

    public void code(String code) {
        String cookieValue = UUID.randomUUID().toString();
        CookieTool.setCookie(COOKIE_NAME, cookieValue, COOKIE_AGE, httpServletRequest, httpServletResponse);
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(REDIS_SESSION_PREFIX + cookieValue);
        ops.set(code.toLowerCase(), 120, TimeUnit.SECONDS);
    }

    public boolean verify(String code) {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        if (cookie == null || "".equals(cookie) || cookie.length() > 256) {
            return false;
        }
        Object o = redisTemplate.boundValueOps(REDIS_SESSION_PREFIX + cookie).get();
        redisTemplate.delete(REDIS_SESSION_PREFIX + cookie);
        CookieTool.cancelCookie(COOKIE_NAME, httpServletRequest, httpServletResponse);
        return o != null && code.toLowerCase().equals(o.toString());
    }

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    private static final String COOKIE_NAME = "_c";
    private static final String REDIS_SESSION_PREFIX = "captcha_";
    private static final int COOKIE_AGE = -1;

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private HttpServletResponse httpServletResponse;
}
