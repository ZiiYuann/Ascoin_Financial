package com.tianli.management.agentmanage.mapper;

import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.currency.CurrencyTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface AgentManageMapper {

    @SelectProvider(type = GenerateSQL.class, method = "getPage")
    List<AgentManagePageDTO> getPage(@Param("nick") String nick,
                                     @Param("username") String username,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

//    @Select("select ifnull(SUM(`balance`), 0) as sumBalance, ifnull(SUM(`remain`), 0) as sumRemain from currency c where type = #{type}  and  EXISTS(select id from agent where id = c.uid and identity = 'senior_agent')")
//    Map<String, BigDecimal> selectSumCurrency(@Param("type") CurrencyTypeEnum type);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumBet")
    Map<String,BigDecimal> selectSumBet(@Param("inSqlString") String inSqlString,
                                        @Param("phone") String phone,
                                        @Param("result") BetResultEnum result,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumCurrency")
    Map<String,BigDecimal> selectSumCurrency(@Param("type") CurrencyTypeEnum type,
                                             @Param("nick") String nick,
                                             @Param("username") String username,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime,
                                             @Param("focus") Boolean focus);

    class GenerateSQL{
        public String selectSumCurrency(CurrencyTypeEnum type,
                                        String nick,
                                        String username,
                                        String startTime,
                                        String endTime,
                                        Boolean focus){
            SQL sql = new SQL()
                    .SELECT("ifnull(SUM(cd.`balance`), 0) as sumBalance")
                    .SELECT("ifnull(SUM(cd.`balance_BF`), 0) as sumBalanceBF")
                    .FROM(" (select * from agent where identity = 'senior_agent') a ")
                    .LEFT_OUTER_JOIN(" user_statistics us on us.id = a.id ")
                    .LEFT_OUTER_JOIN(" currency cd on cd.uid = a.id ")
                    .WHERE("cd.`type` = #{type}");
            if(StringUtils.isNotBlank(nick)){
                sql.WHERE(" a.nick like  concat('%',#{nick},'%')");
            }
            if(StringUtils.isNotBlank(username)){
                sql.WHERE(" a.username like CONCAT('%',#{username},'%')  ");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" a.create_time >= #{startTime} ");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" a.create_time <= #{endTime} ");
            }
            if(Objects.nonNull(focus)){
                sql.WHERE(" a.focus = #{focus} ");
            }
            return sql.toString();
        }

        public String selectSumBet(String inSqlString,
                                         String phone,
                                         BetResultEnum result,
                                         String startTime,
                                         String endTime){
            SQL sql = new SQL()
                    .SELECT(" ifnull(SUM(`amount`),0) as sumBetAmount ")
                    .SELECT(" ifnull(SUM(`agent_dividends`),0) as sumDividendsAmount ")
                    .SELECT(" ifnull(SUM(CASE WHEN `final_BF` > 0 THEN 0 ELSE `agent_profit` END),0) as sumRebateAmount ")
                    .SELECT(" ifnull(SUM(CASE WHEN `final_BF` > 0 THEN `agent_profit` ELSE 0 END),0) as sumRebateAmountBF ")
                    .FROM(" `bet` ");
            if(StringUtils.isNotBlank(inSqlString)){
                sql.WHERE(" `id` in (${inSqlString})");
            }
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE(" `uid_username` like CONCAT('%',#{phone},'%') ");
            }
            if(Objects.nonNull(result)){
                sql.WHERE(" `result` = #{result}");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" `create_time` >= #{startTime} ");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" `create_time` <= #{endTime} ");
            }
            return sql.toString();
        }

        public String getPage(String nick,
                              String username,
                              String startTime,
                              String endTime,
                              int offset,
                              int size){
            SQL sql = new SQL()
                    .SELECT("a.`id` as `id`,a.nick as nick, a.username as username, cd.balance as balance, a.profit as profit, a.profit_BF as profit_BF, cs.balance as unsettlement,  cs.balance_BF as unsettlementBF, a.focus as focus, \n" +
                            "       cd.remain as remain, a.create_time as create_time, a.real_dividends as real_dividends, a.steady_dividends as steady_dividends, a.expect_deposit as expect_deposit, a.normal_rebate_proportion as normal_rebate_proportion,\n" +
                            "       a.steady_rebate_proportion as steady_rebate_proportion,a.expect_dividends as expect_dividends, us.team_number as team_number, us.referral_number as referral_number,\n" +
                            "       a_d.btc as deposit_omni, a_d.eth as deposit_erc20, a_d.tron as deposit_trc20,a_s.btc as settlement_omni, a_s.eth as settlement_erc20, a_s.tron as settlement_trc20,a_s.bsc as settlement_bep20,\n" +
                            "       a.note as note")
                    .FROM(" (select * from agent) a ")
                    .LEFT_OUTER_JOIN(" user_statistics us on us.id = a.id ")
                    .LEFT_OUTER_JOIN(" currency cd on cd.uid = a.id and cd.type = 'deposit' ")
                    .LEFT_OUTER_JOIN(" currency cs on cs.uid = a.id and cs.type = 'settlement' ")
                    .LEFT_OUTER_JOIN(" address a_d on a_d.uid = a.id and a_d.type = 'deposit' ")
                    .LEFT_OUTER_JOIN(" address a_s on a_s.uid = a.id and a_s.type = 'settlement' ");
//            SQL sql = new SQL()
//                    .SELECT("a.`id` as `id`, a.username as username, a.`nick` as `nick`, a.normal_rebate_proportion as normal_rebate_proportion, " +
//                            "a.steady_rebate_proportion as steady_rebate_proportion, a.note as note, a.super_agent as super_agent, " +
//                            "a.create_time as create_time, us.referral_number as referral_number, us.team_number as team_number ")
//                    .FROM("(select * from agent where identity = 'senior_agent') a ").LEFT_OUTER_JOIN(" user_statistics us on us.id = a.id ");
            if(StringUtils.isNotBlank(nick)){
                sql.WHERE(" a.nick like CONCAT('%',#{nick},'%') ");
            }
            if(StringUtils.isNotBlank(username)){
                sql.WHERE(" a.username like CONCAT('%',#{username},'%') ");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" a.create_time >= #{startTime} ");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" a.create_time <= #{endTime} ");
            }
            return sql.ORDER_BY("a.create_time DESC").toString() + " limit " + offset + " , " + size;
        }
    }
}
