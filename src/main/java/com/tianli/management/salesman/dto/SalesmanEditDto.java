package com.tianli.management.salesman.dto;

import cn.hutool.core.util.StrUtil;
import com.tianli.common.Constants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.admin.CreateAdminDTO;
import com.tianli.management.salesman.enums.SalesmanEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author lzy
 * @date 2022/4/6 4:19 下午
 */
@Data
public class SalesmanEditDto {

    private Long id;

    @NotEmpty(message = "请输入用户名")
    @Size(max = 8, message = "用户名最多8个字符")
    private String username;

    @Pattern(regexp = Constants.password_verify_regex, message = "请输入6-16位数字和字母组合的密码")
    private String password;

    private Long leader_id;

    private String remark;


    private String kf_url;


    public CreateAdminDTO getCreateAdminDTO(SalesmanEnum salesmanEnum) {
        if (StrUtil.isBlank(password)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        return CreateAdminDTO.builder()
                .username(username)
                .password(password)
                .operation_password(password)
                .nickname(username)
                .role_name(salesmanEnum.name())
                .build();
    }
}
