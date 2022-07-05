package com.tianli.user.mapper;

/**
 * @author wangqiyun
 * @since 2020/11/2 15:14
 */
public enum UserStatus {
    enable, disable;

    public UserStatus negate(){
        return this == enable ? disable : enable;
    }
}
