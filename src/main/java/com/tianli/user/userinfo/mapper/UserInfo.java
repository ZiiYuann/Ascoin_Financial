package com.tianli.user.userinfo.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 *  用户信息表
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UserInfo {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 账号(缓存)
     */
    private String username;

    /**
     * 账号的地域
     */
    private String region;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

}
