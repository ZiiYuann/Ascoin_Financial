package com.tianli.user.password.controller;

import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author wangqiyun
 * @Date 2020/3/12 16:06
 */
@Data
public class UserPasswordDTO {
    @NotBlank
    private String password;
    @NotBlank
    @Pattern(regexp = Constants.email_verify_regex, message = "请输入正确的邮箱")
    private String phone;
    @NotBlank
    private String code;
    private String old_password;
}
