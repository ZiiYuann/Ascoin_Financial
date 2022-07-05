package com.tianli.captcha.phone.controller.dto;

import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 6:13 PM
 */
@Data
public class PhoneCodeDTO {
    @NotBlank
    private String phone;
    @NotNull
    private CaptchaPhoneType type;

    @NotNull
    private String regionNo;
}
