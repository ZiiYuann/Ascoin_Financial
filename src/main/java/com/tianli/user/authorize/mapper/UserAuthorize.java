package com.tianli.user.authorize.mapper;

import com.tianli.user.authorize.UserAuthorizeType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户推荐表
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UserAuthorize {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 类型
     * @see UserAuthorizeType
     */
    private UserAuthorizeType type;

    /**
     * 唯一标识
     */
    private String openid;

    /**
     * 第三方获取的昵称
     */
    private String name;

    /**
     * 第三方获取的图片
     */
    private String picture;
}
