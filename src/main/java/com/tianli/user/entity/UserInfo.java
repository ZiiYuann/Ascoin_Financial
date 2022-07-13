package com.tianli.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.signature.qual.Identifier;

/**
 * @author chenb
 * @apiNote 用户信息
 * @since 2022-07-13
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    @Identifier
    public long id;

    /**
     * 签名钱包的地址
     */
    private String signAddress;

    /**
     * 签名钱包的链
     */
    private String signChain;

}
