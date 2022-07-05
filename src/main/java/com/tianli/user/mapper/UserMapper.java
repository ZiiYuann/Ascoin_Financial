package com.tianli.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2019-11-06 18:27
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM `user` WHERE `id`=#{id}")
    User get(long id);

    @Select("SELECT * FROM `user` WHERE `referral_code`=#{code} LIMIT 1")
    User selectByReferralCode(String code);

    @Select("SELECT * FROM `user` WHERE `username`=#{username}")
    User getByUsername(String username);

    @Update("UPDATE `user` SET `last_time`=#{last_time},`last_ip`=#{last_ip} WHERE `id`=#{id}")
    long last(@Param("id") long id, @Param("last_time") LocalDateTime last_time, @Param("last_ip") String last_ip);

    @Select("SELECT * FROM `user` WHERE `id` in (${inSqlString}) ORDER BY create_time DESC")
    List<User> getByIds(@Param("inSqlString") String inSqlString);

    @Update("UPDATE `user` SET `status`= #{negate} WHERE `id`= #{id}")
    int updateStatus(@Param("id") long id, @Param("negate") UserStatus negate);

    @Update("UPDATE `user` SET `referral_code`= #{code} WHERE `id`= #{id}")
    int updateRcodeById(@Param("id") long id, @Param("code") String code);

    @Update("UPDATE `user` SET `identity`= #{identity} WHERE `id`= #{id}")
    int updateIdentityById(@Param("id") long id, @Param("identity") UserIdentity identity);

    @Update("UPDATE `user` SET `BF`= #{BFPay} WHERE `id`= #{uid}")
    long updateBFPay(@Param("uid") long uid, @Param("BFPay") boolean BFPay);

    @Select("SELECT * FROM `user` WHERE `hash_key`=#{hashKey}")
    User getByHashKey(String hashKey);

    @Update("UPDATE `user` SET `use_robot`= #{robot} WHERE `id`= #{uid}")
    long updateRobot(@Param("uid") long uid,@Param("robot")  boolean robot);

    @Update("UPDATE `user` SET `auto_count`= #{auto_count}, " +
            "`auto_amount`= #{auto_amount}, " +
            "`interval_time` = #{interval_time}, " +
            "`win_rate` = #{win_rate}, " +
            "`profit_rate` = #{profit_rate} WHERE `id`= #{uid}")
    long updateRobotConfig(@Param("uid") Long uid,
                           @Param("auto_count") Integer auto_count,
                           @Param("auto_amount") BigDecimal auto_amount,
                           @Param("interval_time") String interval_time,
                           @Param("win_rate") Double win_rate,
                           @Param("profit_rate") Double profit_rate);

    @Update("UPDATE `user` SET `auto_count` = `auto_count` - 1 WHERE `id` = #{uid} and `auto_count` > 0")
    long decrementCount(@Param("uid") long uid);

    @Update("UPDATE `user` SET `auto_count` = 5")
    void resetAutoCount();

    @Update("UPDATE `user` SET `node`= #{node} WHERE `id`= #{uid}")
    long updateNode(@Param("uid") Long uid, @Param("node") String node);

    @Update("UPDATE `user` SET `user_type`= #{user_type} WHERE `id`= #{uid}")
    long updateType(@Param("uid") Long uid, @Param("user_type") Integer user_type);

    @Update("UPDATE `user` SET `credit_score`=#{credit_score},`adjust_reason`=#{adjust_reason} WHERE `id`= #{uid}")
    long updateCreditScore(@Param("uid") Long uid, @Param("credit_score") Integer credit_score, @Param("adjust_reason") String adjust_reason);

}
