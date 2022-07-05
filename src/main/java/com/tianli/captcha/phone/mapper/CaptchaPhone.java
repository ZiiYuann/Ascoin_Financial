package com.tianli.captcha.phone.mapper;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 5:32 PM
 */
@Data
public class CaptchaPhone {
    private Long id;
    private LocalDateTime create_time;
    private String phone;
    private CaptchaPhoneType type;
    private String code;
    private String region;
}
