package com.tianli.role.permission.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * permission
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RolePermission {

    /**
     * 角色id
     */
    private Long role_id;

    /**
     * 权限
     */
    private String permission;

}
