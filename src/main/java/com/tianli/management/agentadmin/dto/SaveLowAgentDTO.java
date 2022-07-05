package com.tianli.management.agentadmin.dto;

import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author chensong
 * @date 2021-01-05 09:32
 * @since 1.0.0
 */
@Data
public class SaveLowAgentDTO implements Serializable {

    /**
     * 代理商名称
     */
    @NotBlank(message = "请输入代理商名称")
    @Size(min = 1, max = 8, message = "代理商名称最多输入8个字符")
    private String nick;

    /**
     * 手机号
     */
    @NotBlank(message = "请输入代理商手机号")
    @Pattern(regexp = Constants.phone_verify_regex)
    private String username;

    /**
     * 验证码
     */
    @NotBlank(message = "请输入正确的验证码")
    private String code;

    /**
     * 缴纳保证金数额
     */
    @NotNull(message = "请输入商定缴纳保证金数额")
    private double expect_deposit;

    /**
     * 商定分红
     */
    @NotNull(message = "请输入商定分红占比")
    private double expect_dividends;

    /**
     * 普通场返佣比例
     */
    @NotNull(message = "请输入普通场反佣比例")
    private double normal_rebate_proportion;

    /**
     * 稳赚场返佣比例
     */
    @NotNull(message = "请输入稳赚场反佣比例")
    private double steady_rebate_proportion;

    /**
     * 备注
     */
    @Size(max = 300, message = "备注信息最多输入300个字")
    private String note;
}
