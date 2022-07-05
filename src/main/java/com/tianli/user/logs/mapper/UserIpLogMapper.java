package com.tianli.user.logs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface UserIpLogMapper extends BaseMapper<UserIpLog> {

    @Select("select sum(CASE when (behavior = '验证码登录' OR behavior = '密码登录') and grc_result = 1 then 1 else 0 end) as login_success, \n" +
            "       sum(CASE when (behavior = '验证码登录' OR behavior = '密码登录') and grc_result = 0 then 1 else 0 end) as login_fail, \n" +
            "       sum(CASE when behavior = '下注' and grc_result = 1 then 1 else 0 end) as bet_success, \n" +
            "       sum(CASE when behavior = '下注' and grc_result = 0 then 1 else 0 end) as bet_fail, \n" +
            "       sum(CASE when behavior = '提现' and grc_result = 1 then 1 else 0 end) as withdrawal_success, \n" +
            "       sum(CASE when behavior = '提现' and grc_result = 0 then 1 else 0 end) as withdrawal_fail \n" +
            " from user_ip_log \n" +
            " where username = #{username}")
    Map<String, BigDecimal> selectKindCount(@Param("username") String username);

    @Select("select count(*) as withdrawal_same_ip \n" +
            "from user_ip_log \n" +
            "where `username` != #{username} " +
            "and `ip` = #{ip} and `behavior` = '提现'")
    Map<String, Long> selectIpOtherCount(@Param("ip") String ip, @Param("username") String uid_username);

    @Select("select count(*) as withdrawal_same_equipment \n" +
            "from user_ip_log \n" +
            "where `username` != #{username} " +
            "and `equipment` = #{equipment} and `behavior` = '提现'")
    Map<String, Long> selectEquipmentOtherCount(@Param("equipment") String equipment, @Param("username") String uid_username);
}
