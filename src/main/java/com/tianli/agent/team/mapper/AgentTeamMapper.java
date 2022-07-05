package com.tianli.agent.team.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.tool.time.TimeTool;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 代理商团队表(缓存表) Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface AgentTeamMapper extends BaseMapper<AgentTeam> {

    @Select("select count(*) from `agent_team` " +
            "where `referral_id` = #{uid} and `referral_time` between #{start} and #{end}")
    int selectCountWithInterval(@Param("uid") Long uid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT `id` from `agent` where `senior_id` = #{uid} and `id` not in (SELECT `id` from `user_referral` where `referral_id` = #{uid})")
    List<Long> selectLowAgentNotMyReferral(Long uid);

    @Delete("delete from `agent_team` where `referral_id` = #{id}")
    int deleteByReferralId(@Param("id") long id);

    @Select("select statDate1.date as date, ifnull(statData2.count,0) as count\n" +
            "             from (select date_format(adddate(#{pastDays}, INTERVAL @num:=@num+1 DAY),'%Y-%m-%d') as date\n" +
            "             from `config`,(select @num:=-1) t limit 15 ) statDate1\n" +
            "            left join (\n" +
            "                select count(*) as count, date(referral_time) as date from agent_team\n" +
            "            where referral_id = #{uid} and date(referral_time) >= #{pastDays} and date(referral_time) <= #{today} group by date(referral_time)\n" +
            "            ) statData2 on statDate1.date = statData2.date order by date desc")
    List<Map<String, Object>> selectTeamStatNumDaily(@Param("uid") Long uid,
                                                     @Param("today") String today,
                                                     @Param("pastDays") String pastDays);

    @Select("SELECT t1.date, COALESCE(t2.date_total_count, 0) as team_number ,COALESCE(t3.date_total_count, 0) as referral_number" +
            " FROM(" +
            "     SELECT subdate(date_format(#{today},'%Y-%m-%d'), INTERVAL @num:=@num+1 DAY) as date " +
            "     FROM `config`,(select @num:=-1) t limit ${totalNum} " +
            "     ) t1" +
            " LEFT JOIN(" +
            "     SELECT date_format(m.referral_time, '%Y-%m-%d') as date, count(*) as date_total_count " +
            "     FROM agent_team as m  " +
            "     WHERE referral_id = #{uid}" +
            "     AND date(referral_time) >= #{startDate} and date(referral_time) <= #{today}" +
            "     GROUP BY date_format(m.referral_time, '%Y-%m-%d')" +
            "     ) t2" +
            " ON t1.date = t2.date" +
            " LEFT JOIN(" +
            "     SELECT date_format(u.create_time, '%Y-%m-%d') as date,count(*) as date_total_count" +
            "     FROM user_referral as u " +
            "     WHERE referral_id = #{uid} " +
            "     AND date(create_time) >= #{startDate} and date(create_time) <= #{today}" +
            "     GROUP BY date_format(u.create_time, '%Y-%m-%d') " +
            "     ) t3" +
            " ON t1.date = t3.date ORDER BY t1.date DESC LIMIT #{offset},#{size}")
    List<Map<String, Object>> selectTeamIncrementStatDaily(@Param("uid") Long uid,
                                                           @Param("today") String today,
                                                           @Param("startDate") String startDate,
                                                           @Param("totalNum") int totalNum,
                                                           @Param("offset") Integer offset,
                                                           @Param("size") Integer size);

    @SelectProvider(type = GenerateSQL.class, method = "replaceList")
    void replaceList(List<AgentTeam> agentTeamList);

    class GenerateSQL{
        public String replaceList(List<AgentTeam> agentTeamList){
            StringBuilder sql = new StringBuilder("REPLACE INTO agent_team (referral_id, uid, referral_time) values ");
            agentTeamList.forEach(e -> {
                sql.append(String.format("(%d, %d, %s),", e.getReferral_id(), e.getUid(), TimeTool.getDateTimeDisplayString(e.getReferral_time())));
            });
            return sql.deleteCharAt(sql.lastIndexOf(",")).toString();
        }
    }
}
