package com.tianli.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 管理员角色表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT * FROM `role` WHERE `id` = (SELECT  `role_id` FROM `admin_role` WHERE `uid` = #{aid}) ")
    Role selectByAid(@Param("aid") Long aid);

    @Update("UPDATE `role` SET `admin_number` = `admin_number` + 1 WHERE `id` = #{id}")
    Integer adminNumberPlusOne(@Param("id") Long id);

    @Update("UPDATE `role` SET `admin_number` = `admin_number` - 1 WHERE `id` = #{id}")
    Integer adminNumberMinusOne(@Param("id") Long id);
}
