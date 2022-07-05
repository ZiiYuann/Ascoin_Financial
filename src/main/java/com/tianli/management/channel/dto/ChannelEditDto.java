package com.tianli.management.channel.dto;

import cn.hutool.core.util.StrUtil;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.admin.CreateAdminDTO;
import com.tianli.management.channel.enums.ChannelRoleEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author lzy
 * @date 2022/4/6 4:19 下午
 */
@Data
public class ChannelEditDto {

    private Long id;

    @NotEmpty(message = "请输入用户名")
    @Size(max = 8, message = "用户名最多8个字符")
    private String username;

    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位数字和字母组合的密码")
    private String password;

    private Long leader_id;

    private String remark;

    public CreateAdminDTO getCreateAdminDTO() {
        if (StrUtil.isBlank(password)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        return CreateAdminDTO.builder()
                .username(username)
                .password(password)
                .operation_password(password)
                .nickname(username)
                .role_name(ChannelRoleEnum.渠道.name())
                .build();
    }
}
