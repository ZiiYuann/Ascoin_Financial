package com.tianli.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LineLoginDTO {
    @NotBlank(message = "请输入正确的手机号")
    private String phone;
    @NotBlank(message = "请输入正确的验证码")
    private String code;
    @NotBlank(message = "临时Token不能为空")
    private String tempToken;
}
