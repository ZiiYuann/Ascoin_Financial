package com.tianli.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.management.agentmanage.controller.AgentRakeDetailDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 代理商表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface AgentMapper extends BaseMapper<Agent> {

    @Update("update `agent` set `profit` = case when #{token} = 'usdt' then `profit` + #{profit} else `profit` end, \n" +
            "`profit_BF` = case when #{token} = 'BF' then `profit_BF` + #{profit} else `profit_BF` end\n" +
            "where id = #{uid}")
    void increaseProfit(@Param("uid") long uid, @Param("token") CurrencyTokenEnum token, @Param("profit") BigInteger profit);

    @Update("UPDATE  `agent` SET `settled_number` = `settled_number` + #{settledNumber} WHERE `id` = #{uid}")
    void increaseSettledNumber(@Param("uid") long uid, @Param("settledNumber") BigInteger settledNumber);

    @Update("UPDATE agent SET super_agent = TRUE WHERE id IN ( " +
            "   SELECT re.id FROM ( " +
            "       SELECT aus1.id ,GROUP_CONCAT(aus2.id) , COUNT(aus2.id) AS low_number FROM  " +
            "           (SELECT a1.id FROM agent a1 LEFT JOIN user_statistics us1 ON a1.id = us1.id WHERE us1.referral_number >= #{super_agent_referral}) aus1 " +
            "       Left JOIN " +
            "           (SELECT a2.id, a2.senior_id FROM agent a2 LEFT JOIN user_statistics us2 ON a2.id = us2.id WHERE us2.referral_number >= #{super_agent_subordinate_referral}) aus2 " +
            "       ON aus1.id = aus2.senior_id GROUP BY aus1.id ) re " +
            "   WHERE low_number >= #{super_agent_subordinate} " +
            ")")
    long updateSuperAgent(@Param("super_agent_referral") Integer super_agent_referral,
                          @Param("super_agent_subordinate") Integer super_agent_subordinate,
                          @Param("super_agent_subordinate_referral") Integer super_agent_subordinate_referral);

    @SelectProvider(type = GenerateSQL.class, method ="rakeDetail")
    List<AgentRakeDetailDTO> rakeDetail(@Param("phone") String phone,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime,
                                        @Param("offset") Integer offset,
                                        @Param("size") Integer size);

    @SelectProvider(type = GenerateSQL.class, method ="countRake")
    long rakeDetailCount(@Param("phone") String phone,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
    class GenerateSQL{
        private SQL countSQL(String phone, String startTime, String endTime){
            SQL sql = new SQL().SELECT("count(1)")
                    .FROM("`agent` AS a")
                    .RIGHT_OUTER_JOIN("`currency_log` AS cl ON a.id = cl.uid")
                    .WHERE("cl.`des` = '抽水'");
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE("a.`username` like CONCAT('%',#{phone},'%')");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE("cl.`create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE("cl.`create_time` <= #{endTime}");
            }
            return sql;
        }

        public String countRake(@Param("phone") String phone,
                                @Param("startTime") String startTime,
                                @Param("endTime") String endTime){
            return countSQL(phone, startTime, endTime).toString();
        }

        private SQL selectSQL(String phone, String startTime, String endTime, Integer offset, Integer size){
            SQL sql = new SQL().SELECT("a.id,a.nick,a.username ,cl.create_time, cl. amount")
                    .FROM("`agent` AS a")
                    .RIGHT_OUTER_JOIN("`currency_log` AS cl ON a.id = cl.uid")
                    .WHERE("cl.`des` = '抽水'");
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE("a.`username` like CONCAT('%',#{phone},'%')");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE("cl.`create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE("cl.`create_time` <= #{endTime}");
            }
            return sql.ORDER_BY("cl.`create_time` DESC").LIMIT("#{offset}, #{size}");
        }

        public String rakeDetail(@Param("phone") String phone,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime,
                                 @Param("offset") Integer offset,
                                 @Param("size") Integer size){
            return selectSQL(phone, startTime, endTime, offset, size).toString();
        }
    }
}
