package com.tianli.admin;

import com.tianli.admin.mapper.AdminStatus;
import com.tianli.role.mapper.Role;
import com.tianli.role.permission.mapper.RolePermission;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 管理员
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class AdminAndRoles {

    /**
     * 主键
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 禁用状态
     */
    private AdminStatus status;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色
     */
    private Role role;

    /**
     * 角色
     */
    private List<RolePermission> rolePermissionList;



}
