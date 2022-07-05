package com.tianli.admin.role.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 管理员角色表
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class AdminRole {

    /**
     * 管理员id
     */
    private Long uid;

    /**
     * 角色id
     */
    private Long role_id;

}
