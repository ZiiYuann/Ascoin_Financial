package com.tianli.management.captchalist.controller;

import com.tianli.captcha.phone.mapper.CaptchaPhone;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </P>
 *
 * @author linyifan
 * @since 7/5/21 12:00 PM
 */

@Data
@Builder
public class CaptchaListVO {

    private Long id;
    private LocalDateTime create_time;
    private String phone;
    private String code;

    public static CaptchaListVO trans(CaptchaPhone captchaPhone){
        return CaptchaListVO.builder()
                .id(captchaPhone.getId())
                .create_time(captchaPhone.getCreate_time())
                .phone(captchaPhone.getPhone())
                .code(captchaPhone.getCode()).build();
    }

}
