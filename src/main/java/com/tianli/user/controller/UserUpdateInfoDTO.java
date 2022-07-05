package com.tianli.user.controller;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wangqiyun
 * @Date 2019-12-30 20:04
 */

@Data
public class UserUpdateInfoDTO {
    @NotNull
    private String nick;
}
