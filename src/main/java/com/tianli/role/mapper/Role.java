package com.tianli.role.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

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
public class Role {

    /**
     * 角色id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色状态
     */
    private String status;

    /**
     * 管理员数
     */
    private Long admin_number;

    /**
     * 备注
     */
    private String note;

}
