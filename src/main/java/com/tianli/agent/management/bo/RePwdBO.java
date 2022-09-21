package com.tianli.agent.management.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RePwdBO {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

}
