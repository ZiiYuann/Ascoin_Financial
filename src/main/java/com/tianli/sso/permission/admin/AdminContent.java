package com.tianli.sso.permission.admin;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 用户信息上下文
 * @author haoding
 */
@Slf4j
public class AdminContent {

    private static ThreadLocal<AdminInfo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取管理员信息
     */
    public static AdminInfo get() {
        AdminInfo user = THREAD_LOCAL.get();
        if(Objects.isNull(user)){
            user = new AdminInfo();
            THREAD_LOCAL.set(user);
        }
        return user;
    }

    /**
     * 移除管理员信息
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

}