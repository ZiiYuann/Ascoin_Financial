package com.tianli.role.privilege.mapper;

import lombok.Data;

/**
 * @author chensong
 * @date 2020-12-29 16:06
 * @since 1.0.0
 */
@Data
public class RolePrivilegeList {
    private Long id;
    private String name;
    private Long parent_id;
    private Integer sort;
}
