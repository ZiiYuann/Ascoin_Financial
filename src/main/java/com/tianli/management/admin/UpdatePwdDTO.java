package com.tianli.management.admin;

import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author wangqiyun
 * @Date 2018/9/26 下午3:35
 */
@Data
public class UpdatePwdDTO {
    /**
     * 0-登录密码 1-操作密码
     */
    private Integer type = 0;
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位由数字和字母组合的登录旧密码")
    private String oldPassword;

    /**
     * 密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位由数字和字母组合的登录新密码")
    private String password;

}
