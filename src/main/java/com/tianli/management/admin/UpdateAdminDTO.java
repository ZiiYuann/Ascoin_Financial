package com.tianli.management.admin;

import com.tianli.common.Constants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author chensong
 * @date 2021-01-04 20:25
 * @since 1.0.0
 */
@Data
public class UpdateAdminDTO {
    /**
     * 管理员id
     */
    private Long id;
    /**
     * 用户名
     */
    @NotEmpty(message = "请输入用户名")
    @Size(max = 8 , message = "用户名最多8个字符")
    private String username;

    /**
     * 昵称
     */
    private String nickname;
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
    @Size(max = 300 , message = "备注信息最多输入300个字")
    private String note;
}
