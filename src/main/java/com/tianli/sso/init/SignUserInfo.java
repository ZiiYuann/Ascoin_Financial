package com.tianli.sso.init;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUserInfo {
    /**
     * token
     */
    private String token;
    /**
     * token的失效时间戳
     */
    private Long invalidTime;
    /**
     * 签名钱包的地址
     */
    private String signAddress;
    /**
     * 签名钱包的链
     */
    private String signChain;
    /**
     * 签名的类型
     */
    private SignWalletType signType;

    /**
     * 签名的用户信息
     */
    private Long uid;
    /**
     * 用户的创建时间
     */
    private LocalDateTime createTime;
    /**
     * 用户的eth地址
     */
    private String address;
    /**
     * 用户的邀请码
     */
    private String inviteCode;
    /**
     * 用户的此次登录的ip
     */
    private String ip;
    /**
     * 用户的此次登录的地区
     */
    private String region;
    /**
     * 渠道id
     */
    private Long channel_id;
}
