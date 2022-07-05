package com.tianli.user.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;

/**
 * <p>
 * 用户统计表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-08
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
    @Update("update `user_statistics` set `referral_number` = `referral_number` + #{num} where `id` = #{id}")
    int incrementReferralNum(@Param("id") long id, @Param("num") int num);

    @Update("update `user_statistics` set `team_number` = `team_number` + #{num} where `id` = #{id}")
    int incrementTeamNum(@Param("id") long id, @Param("num") int num);

    @Update("update `user_statistics` set `team_number` = `team_number` + #{num} where `id` in (${inSqlString}) ")
    int incrementTeamNumByIds(@Param("inSqlString") String inSqlString, @Param("num") int num);

    @Update("update `user_statistics` set `referral_number` = `referral_number` + #{num}, `team_number` = `team_number` + #{num} where `id` = #{id}")
    int incrementTeamAndReferralNum(@Param("id") long id, @Param("num") int num);

    @Update("update `user_statistics` set `my_amount` = `my_amount` + #{amount} where `id` = #{id}")
    int incrementMyAmount(@Param("id") long id,  @Param("amount") BigInteger amount);

    @Update("update `user_statistics` set `team_amount` = `team_amount` + #{amount} where `id` = #{id}")
    int incrementTeamAmount(@Param("id") long id,  @Param("amount") BigInteger amount);

    @Update("update `user_statistics` set `team_amount` = `team_amount` + #{amount} where `id` in (${inSqlString}) ")
    int incrementTeamAmountByIds(@Param("inSqlString") String inSqlString,  @Param("amount") BigInteger amount);

    @Update("update `user_statistics` set `rebate` = `rebate` + #{amount} where `id` = #{id}")
    int incrementRebate(@Param("id") long uid, @Param("amount") BigInteger amount);

}
