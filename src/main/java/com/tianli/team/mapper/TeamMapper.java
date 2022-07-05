package com.tianli.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamMapper{

    @Select("select ur.id as id, ui.create_time as create_time, ui.nick as nick, ui.username as username, ui.avatar as avatar, us.my_amount as my_amount, us.team_number as team_number , " +
            "(us.referral_number > 0) as hasSubAgent " +
            "from `user_referral` ur " +
            "left join `user_info` ui on ui.id = ur.id " +
            "left join `user_statistics` us on us.id = ur.id " +
            "where ur.referral_id = #{uid} and ui.id > 0 and us.id > 0 " +
            "order by ur.create_time desc limit #{offset}, #{size}")
    List<TeamReferralPageDTO> selectTeamReferralPage(@Param("uid") Long uid, @Param("offset") Integer offset, @Param("size") Integer size);

    @Select("select a.id as id, a.create_time as create_time, a.nick as nick, a.super_agent as super_agent, " +
            "ui.username as username, ui.avatar as avatar, " +
            "us.team_amount as team_amount, us.team_number as team_number, " +
            "CASE WHEN (select 1 from `agent` where `senior_id` = a.id limit 1) = 1 THEN true ELSE false END as hasSubAgent " +
            "from (select `id`,`create_time`, `nick`, `username`, `super_agent` from `agent` where `senior_id` = #{uid}) a " +
            "left join `user_info` ui on ui.id = a.id " +
            "left join `user_statistics` us on us.id = a.id " +
            "where ui.id > 0 and us.id > 0 " +
            "order by a.create_time desc limit #{offset}, #{size}")
    List<TeamAgentPageDTO> selectTeamAgentPage(@Param("uid") Long uid, @Param("offset") Integer offset, @Param("size") Integer size);
}
