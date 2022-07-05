package com.tianli.user.controller;

import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:58
 */

@Data
public class UserReferralRegDTO {
    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = Constants.email_verify_regex, message = "请输入正确的用户名")
    private String username;
    @NotBlank(message = "请输入验证码")
    private String code;
//    @NotBlank(message = "用户唯一标识为空")
//    private String hash_key;
    @NotBlank(message = "邀请码为空")
    private String referralCode;
}
