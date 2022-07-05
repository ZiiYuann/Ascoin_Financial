package com.tianli.management.agentadmin.mapper;

import com.tianli.bet.mapper.BetResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface AgentAdminMapper {

    @Select("select  " +
            " ifnull(sum(c.balance), 0) as balance, " +
            " ifnull(sum(a.profit), 0) as profit, " +
            " ifnull(sum(cs.balance), 0) as un_settled_number, " +
            " ifnull(sum(us.team_number), 0) as team_number, " +
            " ifnull(sum(us.rebate), 0) as rebate from agent a " +
            "left join user_statistics us on a.id = us.id " +
            "left join currency c on a.id = c.uid and c.type = 'deposit' " +
            "left join currency cs on a.id = cs.uid and cs.type = 'settlement' " +
            "where exists (select id from agent where senior_id = #{seniorId} and id = a.id)")
    HomeStatDataDTO selectLowStatBySeniorId(long seniorId);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumDividends")
    Map<String, BigDecimal> selectSumDividends(@Param("uid") Long uid,
                                               @Param("phone") String phone,
                                               @Param("result") BetResultEnum result,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);
    class GenerateSQL{

        public String selectSumDividends(String phone,
                                         BetResultEnum result,
                                         String startTime,
                                         String endTime){
            SQL sql = new SQL()
                    .SELECT(" SUM(`amount`) as amountSum, " +
                            "SUM(CASE WHEN `profit_token` = 'BF' THEN `my_profit` ELSE 0 END) as myProfitSumBF, " +
                            "SUM(CASE WHEN `profit_token` = 'usdt' THEN `my_profit` ELSE 0 END) as myProfitSum ")
                    .FROM("`dividends`").WHERE("`dividends_uid` = #{uid}");
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE(" `uid_username` like CONCAT('%',#{phone},'%')");
            }
            if(Objects.nonNull(result)){
                sql.WHERE(" `result` = #{result}");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" `create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" `create_time` <= #{endTime}");
            }
            return sql.toString();
        }
    }
}
