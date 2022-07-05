package com.tianli.tool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

/**
 * Created by wangqiyun on 2017/5/23.
 */
public class CookieTool {
    public static String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }

    public static void cancelCookie(@NotBlank String cookieName, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath(getCookiePath(request));
        response.addCookie(cookie);
    }

    public static String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key))
                    return cookie.getValue();
            }
        }
        return null;
    }

    public static void setMaxageCookie(@NotBlank String cookieName, @NotBlank String cookieValue, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(true);
        if (request.isSecure() || "https".equals(request.getHeader("x-scheme")))
            cookie.setSecure(true);
        response.addCookie(cookie);
    }

    public static void setCookie(@NotBlank String cookieName, @NotBlank String cookieValue, int age, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(age);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(true);
        if (request.isSecure() || "https".equals(request.getHeader("x-scheme")))
            cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
