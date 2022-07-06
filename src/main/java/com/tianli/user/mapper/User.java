package com.tianli.user.mapper;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2019-11-06 17:39
 */
@Data
public class User {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    /**
     * 账号
     */
    private String username;
    /**
     * 身份
     */
    private UserIdentity identity;
    /**
     * 状态
     */
    private UserStatus status;
    /**
     * 上次访问时间
     */
    private LocalDateTime last_time;
    /**
     * 上次访问ip
     */
    private String last_ip;

    /**
     * 备注
     */
    private String node;
}
