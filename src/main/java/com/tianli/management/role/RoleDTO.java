package com.tianli.management.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {

    /**
     * 角色id
     */
    private Long id;

    /**
     * 角色名称
     */
    @NotEmpty(message = "请输入角色名称")
    @Size(max = 8,message = "角色名称最多输入8个字符")
    private String name;

    /**
     * 备注
     */
    private String note;

    /**
     * 权限
     */
    @NotEmpty(message = "请输入角色权限")
    private List<String> permission;
}
