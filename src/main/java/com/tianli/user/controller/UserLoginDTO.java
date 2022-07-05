package com.tianli.user.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 11:28
 */

@Data
public class UserLoginDTO {
    @NotBlank
    @Size(max = 32)
    private String username;
    @NotBlank
    @Size(max = 256)
    private String password;

//    @NotBlank(message = "用户唯一标识为空")
//    private String hash_key;

}
