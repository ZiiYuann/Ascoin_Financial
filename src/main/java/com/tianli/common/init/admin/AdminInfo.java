package com.tianli.common.init.admin;

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
}
