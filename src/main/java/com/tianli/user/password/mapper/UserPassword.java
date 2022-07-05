package com.tianli.user.password.mapper;

import lombok.Data;

/**
 * @Author wangqiyun
 * @Date 2020/3/11 17:12
 */

@Data
public class UserPassword {
    private Long id;
    private String login_password;
    private String pay_password;
}
