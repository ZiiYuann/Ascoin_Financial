package com.tianli.user.password.mapper;

import org.apache.ibatis.annotations.*;

/**
 * @Author wangqiyun
 * @Date 2020/3/11 17:30
 */

@Mapper
public interface UserPasswordMapper {
    @Select("SELECT * FROM `user_password` WHERE `id`=#{id}")
    UserPassword get(long id);

    @Insert("INSERT INTO `user_password`(`id`) VALUES (#{id})")
    long insert(long id);

    @Update("UPDATE `user_password` SET `login_password`=#{login_password} WHERE `id`=#{id}")
    long updateLoginPassword(@Param("id") long id, @Param("login_password") String login_password);

    @Update("UPDATE `user_password` SET `pay_password`=#{pay_password} WHERE `id`=#{id}")
    long updatePayPassword(@Param("id") long id, @Param("pay_password") String pay_password);
}
