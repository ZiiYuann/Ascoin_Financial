package com.tianli.role.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * permission Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    @Select("SELECT `permission` FROM `role_permission` WHERE `role_id` = #{id}")
    List<String> selectPermissionList(@Param("id") Long id);
}
