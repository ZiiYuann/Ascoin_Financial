package com.tianli.management.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleVO {

    /**
     * 角色id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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
    private Long adminNumber;

    /**
     * 备注
     */
    private String note;


    /**
     * 权限
     */
    private List<String> permission;
}
