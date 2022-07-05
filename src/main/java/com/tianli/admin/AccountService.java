package com.tianli.admin;

import com.tianli.tool.CookieTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AccountService {

    public void login(long id) {
        this.setAttribute(id);
    }

    public Long getLogin() {
        return this.getAttribute();
    }

    public void logout() {
        this.invalidate();
    }


    private Long getAttribute() {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        if(StringUtils.isBlank(cookie) || cookie.length() > 256){
            // cookie不存时获取请求头中信息
            Object headerSession = httpServletRequest.getHeader(SESSION_TMP);
            if(Objects.isNull(headerSession)){
                return null;
            }
            String sessionInfo = headerSession.toString();
            String userIdStr = sessionInfo.split("-")[0];
            cookie = sessionInfo.replaceAll(userIdStr + "-","");
        }
        // 查询redis是否存在用户信息
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(REDIS_SESSION_PREFIX + cookie);
        Object o = ops.get();
        if(Objects.isNull(o)){
            // redis缓存不存在则清除cookie, 重新登录
            CookieTool.cancelCookie(COOKIE_NAME, httpServletRequest, httpServletResponse);
            httpServletResponse.setHeader(SESSION_TMP, null);
            return null;
        }
        Long userId = Long.valueOf(o.toString());
        // 更新缓存和cookie的过期时间
        ops.set(userId, cookieAge, TimeUnit.SECONDS);
        CookieTool.setCookie(COOKIE_NAME, cookie, cookieAge, httpServletRequest, httpServletResponse);

        // 返回用户id
        return userId;
    }

    /**
     * 登录时 设置cookie, redis, header 信息
     * @param value
     */
    private void setAttribute(long value) {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        if (cookie == null || "".equals(cookie) || cookie.length() > 256) {
            cookie = UUID.randomUUID().toString();
        }
        CookieTool.setCookie(COOKIE_NAME, cookie, cookieAge, httpServletRequest, httpServletResponse);
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(REDIS_SESSION_PREFIX + cookie);
        ops.set(value, cookieAge, TimeUnit.SECONDS);
        // 属性中也存一份, 防止部分浏览器禁用cookie时的无限登录
        httpServletResponse.setHeader(SESSION_TMP, value + "-" + cookie);
    }

    /**
     * 删除服务器缓存和请求cookie
     */
    private void invalidate() {
        String cookie = CookieTool.getCookie(httpServletRequest, COOKIE_NAME);
        if(StringUtils.isBlank(cookie)){
            // 本地禁止cookie的情况, 获取属性中存在的信息
            Object sessionHeadInfo = httpServletRequest.getHeader(SESSION_TMP);
            if(Objects.nonNull(sessionHeadInfo)){
                String sessionInfo = sessionHeadInfo.toString();
                String userIdStr = sessionInfo.split("-")[0];
                cookie = sessionInfo.replaceAll(userIdStr + "-","");
            }
        }
        redisTemplate.delete(REDIS_SESSION_PREFIX + cookie);
        CookieTool.cancelCookie(COOKIE_NAME, httpServletRequest, httpServletResponse);
        httpServletResponse.setHeader(SESSION_TMP, null);
    }

    private static final String COOKIE_NAME = "_r";
    private static final String SESSION_TMP = "_r";
    private static final String REDIS_SESSION_PREFIX = "session_";
    /**
     * 过期时间设置为半小时
     */
    @Value("${cookie.age}")
    private int cookieAge = 24 * 60 * 60;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private HttpServletResponse httpServletResponse;
}
