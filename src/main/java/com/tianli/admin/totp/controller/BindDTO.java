package com.tianli.admin.totp.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author wangqiyun
 * @Date 2020/6/1 18:23
 */

@Data
public class BindDTO {
    @NotBlank
    @Size(min = 32, max = 32)
    private String secret;
    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}
