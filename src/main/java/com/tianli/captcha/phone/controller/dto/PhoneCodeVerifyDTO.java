package com.tianli.captcha.phone.controller.dto;

import com.tianli.captcha.phone.mapper.CaptchaPhoneType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Author wangqiyun
 * @Date 2018/12/7 10:05 AM
 */
public class PhoneCodeVerifyDTO {
    @NotBlank
    private String phone;
    @NotNull
    private CaptchaPhoneType type;
    @NotBlank
    @Size(max = 10)
    private String code;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CaptchaPhoneType getType() {
        return type;
    }

    public void setType(CaptchaPhoneType type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
