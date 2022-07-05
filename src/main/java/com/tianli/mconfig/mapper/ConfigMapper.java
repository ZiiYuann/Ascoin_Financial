package com.tianli.mconfig.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 17:33
 */

@Mapper
public interface ConfigMapper {
    @Select("SELECT * FROM `config` WHERE `name`=#{name}")
    Config get(String name);

    @Insert("REPLACE INTO `config`(`name`, `value`) VALUES (#{name},#{value})")
    int insert(Config config);

    @Update("UPDATE `config` SET `value`=#{newValue} WHERE `name`=#{name} AND `value`=#{oldValue}")
    long update(@Param("name") String name, @Param("oldValue") String oldValue, @Param("newValue") String newValue);

    @Update("UPDATE `config` SET `value` = #{value} WHERE `name` = #{name}")
    long updateParam(@Param("name") String name, @Param("value") String value);

    @Insert("REPLACE INTO `config`(`name`, `value`) VALUES (#{name},#{value})")
    void replaceParam(@Param("name") String name, @Param("value") String value);

    @Select("SELECT `value` FROM `config` WHERE `name` = #{name}")
    String getParam(@Param("name") String name);

    @Select("SELECT `name`, `value` FROM `config`")
    List<Config> getAllParams();
}
