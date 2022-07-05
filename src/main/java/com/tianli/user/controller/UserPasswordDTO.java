package com.tianli.user.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author wangqiyun
 * @Date 2019-12-30 20:11
 */
@Data
public class UserPasswordDTO {
    @NotBlank
    private String old_password;
    @NotBlank
    private String new_password;
}
