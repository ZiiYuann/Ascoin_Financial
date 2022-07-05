package com.tianli.admin.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 管理员
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Admin {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 账号
     */
    private String username;

    /**
     * 账号
     */
    private String password;
    /**
     * 操作密码
     */
    private String operation_password;
    /**
     * 禁用状态
     */
    private AdminStatus status;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 备注
     */
    private String note;

    /**
     * 上次访问ip
     */
    private String last_ip;
}
