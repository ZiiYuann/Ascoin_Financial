package com.tianli.role.privilege.mapper;

import com.tianli.management.role.PrivilegeListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author chensong
 * @date 2020-12-29 16:08
 * @since 1.0.0
 */
@Mapper
public interface RolePrivilegeListMapper {
    @Select("SELECT `id`,`name`,`parent_id` FROM role_privilege_list WHERE `parent_id` = 0")
    List<PrivilegeListVO> selectParent();

    @Select("SELECT `id`,`name`,`parent_id` FROM role_privilege_list WHERE `parent_id` = #{id}")
    List<PrivilegeListVO> selectSecond(@Param("id") Long id);
}
