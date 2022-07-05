package com.tianli.management.user.controller;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CustomerUpdateTypeDTO {
    @NotNull(message = "用户类型不能为空")
    private Integer user_type;
}
