package com.tianli.management.bul;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BulAddReq {
    @NotBlank(message = "强杀账号不能为空")
    private String username;
    private int control;
    private String node;
}
