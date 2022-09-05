package com.tianli.agent.management.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthUserBO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码uuid不能为空")
    private String uuid;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
