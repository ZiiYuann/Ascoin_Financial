package com.tianli.user.service;

import com.tianli.user.entity.UserInfo;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
public interface UserInfoService {

    /**
     * 获取通过私钥登入的用户信息
     * @param signAddress 签名链地址
     * @param signChain 链类型
     */
    UserInfo getBySignInfo(String signAddress,String signChain);
}
