package com.tianli.management.admin;

import com.tianli.common.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author chensong
 * @date 2020-12-18 15:44
 * @since 1.0.0
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CreateAdminDTO {

    /**
     * 用户名
     */
    @NotEmpty(message = "请输入用户名")
    @Size(max = 8, message = "用户名最多8个字符")
    private String username;

    /**
     * 昵称
     */
    private String nickname;
    /**
     * 初始密码
     */
    @NotEmpty(message = "请输入密码")
    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位数字和字母组合的密码")
    private String password;

    @NotEmpty(message = "请输入操作密码")
    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位数字和字母组合的密码")
    private String operation_password;
    /**
     * 手机号
     */
    @Pattern(regexp = Constants.phone_verify_regex, message = "请输入正确的手机号")
    private String phone;
    /**
     * 角色名称
     */
    @NotEmpty(message = "请选择角色名称")
    private String role_name;
    /**
     * 备注
     */
    @Size(max = 300, message = "备注信息最多输入300个字")
    private String note;

    @Tolerate
    public CreateAdminDTO() {
    }
}
