package com.tianli.management.agentmanage.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class UpdateAgentDTO implements Serializable {

    /**
     * 代理商id
     */
    @NotNull(message = "代理商主键不能为空")
    private Long id;

    /**
     * 代理商名称
     */
    @NotBlank(message = "请输入代理商名称")
    @Size(min = 1, max = 8, message = "代理商名称最多输入8个字符")
    private String nick;

    /**
     * 代理商手机号
     */
    @NotBlank(message = "请输入代理商UID")
    private String username;

    /**
     * 验证码
     */
    private String code;

    /**
     * 期望押金
     */
//    private double expect_deposit;

    /**
     * 商定分红
     */
    @NotNull(message = "请输入商定分红占比")
    private double expect_dividends;

    /**
     * 商定分红
     */
    @NotNull(message = "请输入商定分红占比")
    private double steady_dividends;

    /**
     * 普通场返佣比例
     */
//    @NotNull(message = "请输入反佣比例")
//    private double normal_rebate_proportion;

    /**
     * 稳赚场返佣比例
     */
//    @NotNull(message = "请输入反佣比例")
//    private double steady_rebate_proportion;

    /**
     * 备注
     */
    @Size(max = 300, message = "备注信息最多输入300个字")
    private String note;
}
