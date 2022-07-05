package com.tianli.captcha.email.controller.dto;

import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 6:13 PM
 */
@Data
public class EmailCodeDTO {
    @NotBlank
    @Pattern(regexp = Constants.email_verify_regex, message = "email格式错误")
    private String email;

    @NotNull
    private CaptchaPhoneType type;
}
