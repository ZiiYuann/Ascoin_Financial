package com.tianli.admin.totp.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author wangqiyun
 * @Date 2020/6/1 18:25
 */
@Data
public class UnBindDTO {
    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}
