package com.tianli.user.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:58
 */

@Data
public class UserUpdateRegionDTO {
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "地域编码不能为空")
    private String region;
}
