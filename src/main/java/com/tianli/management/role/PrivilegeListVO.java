package com.tianli.management.role;

import lombok.Data;

import java.util.List;

/**
 * @author chensong
 * @date 2020-12-29 16:44
 * @since 1.0.0
 */
@Data
public class PrivilegeListVO {
    private Long id;
    private String name;
    private Long parent_id;
    private List<PrivilegeListVO> list;
}
