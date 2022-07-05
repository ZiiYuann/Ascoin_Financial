package com.tianli.common.init.admin;

import com.tianli.role.mapper.Role;
import lombok.Data;

@Data
public class AdminInfo {

    /**
     * admin主键
     */
    private Long aid;

    /**
     * admin账号
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;
    private Role role;
}
