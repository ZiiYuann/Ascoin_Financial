package com.tianli.management.admin;

import com.tianli.common.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author chensong
 * @date 2021-01-06 16:22
 * @since 1.0.0
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AdminUpdatePwdDTO {

    /**
     * 0-登录密码 1-操作密码
     */
    @Builder.Default
    private Integer type = 0;
    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位数字和字母组合的密码")
    private String password;

    @Tolerate
    public AdminUpdatePwdDTO(){}
}
