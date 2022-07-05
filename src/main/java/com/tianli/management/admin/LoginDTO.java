package com.tianli.management.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author wangqiyun
 * @Date 2018/9/26 下午3:35
 */
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "请输入用户名")
    @Size(max = 30)
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    @Size(max = 16, min = 6)
    private String password;
    /**
     * 验证码
     */
    @NotBlank(message = "请输入验证码")
    private String code;

    private String google_code;
}
