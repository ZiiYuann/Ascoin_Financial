package com.tianli.user.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 10:58
 */

@Data
public class UserInitPwdDTO {
//    @NotBlank
//    private String code;
    @NotBlank
    private String password;
    private String referral_code;
}
