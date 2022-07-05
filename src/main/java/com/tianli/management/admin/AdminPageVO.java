package com.tianli.management.admin;

import com.tianli.admin.mapper.AdminStatus;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author chensong
 * @date 2020-12-18 17:55
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class AdminPageVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 账号
     */
    private String username;

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
     * 角色
     */
    private String role_name;

    /**
     * 上次访问ip
     */
    private String last_ip;

}