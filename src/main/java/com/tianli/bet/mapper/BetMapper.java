package com.tianli.bet.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.platformfinance.FinanceDailyBreakdownDTO;
import com.tianli.management.platformfinance.FinanceDailyBreakdownDetailsDTO;
import com.tianli.management.platformfinance.FinanceExhibitionDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 押注表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface BetMapper extends BaseMapper<Bet> {
    // ("SELECT SUM(`profit`) from `bet` WHERE `uid` = #{uid} and `result` = #{result}")

    @SelectProvider(type = GenerateSQL.class, method = "selectSumProfit")
    Map<String, BigDecimal> selectSumProfit(@Param("uid") long uid,
                               @Param("betType") BetTypeEnum betType,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime,
                               @Param("result") BetResultEnum result);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumAmount")
    BigInteger selectSumAmount(@Param("uid") long uid,
                               @Param("betType") BetTypeEnum betType,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime,
                               @Param("result") BetResultEnum result);

    @SelectProvider(type = GenerateSQL.class, method = "betStatistics")
    Map<String, BigDecimal> betStatistics(@Param("phone") String phone,
                                          @Param("result") BetResultEnum result,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);
    @SelectProvider(type = GenerateSQL.class, method = "betStatistics2")
    Map<String, BigDecimal> betStatistics2(@Param("ip") String ip,
                                          @Param("equipment") String equipment,
                                          @Param("grc_result") Boolean grc_result,
                                          @Param("uid") Long uid,
                                          @Param("betType") BetTypeEnum betType,
                                          @Param("phone") String phone,
                                          @Param("result") BetResultEnum result,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime,
                                           @Param("userIds")  Set<Long> userIds);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumAmountByPlatform")
    BigInteger selectSumAmountByPlatform(@Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("result") BetResultEnum result,
                                         @Param("phone") String phone);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumPlatformProfit")
    BigInteger selectSumPlatformProfit(@Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("result") BetResultEnum result,
                                         @Param("phone") String phone);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumAgentDividends")
    BigInteger selectSumAgentDividends(@Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("result") BetResultEnum result,
                                         @Param("phone") String phone);

    @Select("SELECT t1.date, " +
            "COALESCE(t2.settled_number, 0) as settled_number , " +
            "COALESCE(t3.platform_profit, 0) as platform_profit, " +
            "COALESCE(t3.agent_dividends, 0) as agent_dividends, " +
            "COALESCE(t4.rebate_amount, 0) as rebate_amount , " +
            "COALESCE(t5.erc20, 0) as userFeeErc20 , " +
            "COALESCE(t5.omni, 0) as userFeeOmni , " +
            "COALESCE(t6.erc20, 0) as depositFeeErc20 , " +
            "COALESCE(t6.omni, 0) as depositFeeOmni , " +
            "COALESCE(t7.erc20, 0) as settlementFeeErc20 , " +
            "COALESCE(t7.omni, 0) as settlementFeeOmni " +
            " FROM(" +
            "     SELECT adddate(date_format(#{startTime},'%Y-%m-%d'), INTERVAL @num:=@num+1 DAY) as date " +
            "     FROM `k_line`,(select @num:=-1) t WHERE adddate(date_format(#{startTime},'%Y-%m-%d'), INTERVAL @num DAY) < date_format(#{endTime},'%Y-%m-%d') " +
            " ) t1 " +
            " LEFT JOIN( " +
            "     SELECT date_format(m.create_time, '%Y-%m-%d') as date," +
            "            ifnull(SUM(`settled_number`),0) settled_number " +
            "     FROM `agent` as m " +
            "     WHERE m.create_time <= #{endTime} AND m.create_time >= #{startTime} " +
            "           AND `identity` = 'senior_agent' " +
            "     GROUP BY date" +
            " ) t2 " +
            " ON t1.date = t2.date " +
            " LEFT JOIN( " +
            "     SELECT date_format(u.create_time, '%Y-%m-%d') as date," +
            "           ifnull(SUM(`fee`),0) fee, " +
            "           ifnull(SUM(`platform_profit`),0) platform_profit, " +
            "           ifnull(SUM(`agent_dividends`),0) agent_dividends " +
            "     FROM `bet` as u " +
            "     WHERE u.create_time <= #{endTime} AND u.create_time >= #{startTime} " +
            "     GROUP BY date" +
            " ) t3 " +
            " ON t1.date = t3.date " +
            " LEFT JOIN( " +
            "     SELECT date_format(k.create_time, '%Y-%m-%d') as date," +
            "            ifnull(SUM(`rebate_amount`),0) rebate_amount" +
            "     FROM `rebate` as k " +
            "     WHERE k.create_time <= #{endTime} AND k.create_time >= #{startTime} " +
            "     GROUP BY date " +
            " ) t4 " +
            " ON t1.date = t4.date " +
            " LEFT JOIN( " +
            "     SELECT date_format(c.create_time, '%Y-%m-%d') as date," +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END),0) as erc20 , " +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END),0) as omni " +
            "     FROM charge as c " +
            "     WHERE c.create_time <= #{endTime} AND c.create_time >= #{startTime} " +
            "            AND c.charge_type = 'withdraw' AND c.status = 'chain_success' " +
            "     GROUP BY date" +
            " ) t5 " +
            " ON t1.date = t5.date " +
            " LEFT JOIN( " +
            "     SELECT date_format(cd.create_time, '%Y-%m-%d') as date," +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END),0) as erc20 , " +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END),0) as omni " +
            "     FROM charge_deposit as cd " +
            "     WHERE cd.create_time <= #{endTime} AND cd.create_time >= #{startTime}" +
            "            AND cd.charge_type = 'withdraw' AND cd.status = 'transaction_success' " +
            "     GROUP BY date" +
            " ) t6 " +
            " ON t1.date = t6.date " +
            " LEFT JOIN( " +
            "     SELECT date_format(cs.create_time, '%Y-%m-%d') as date," +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END),0) as erc20 , " +
            "            ifnull(sum(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END),0) as omni " +
            "     FROM charge_settlement as cs " +
            "     WHERE cs.create_time <= #{endTime} AND cs.create_time >= #{startTime} " +
            "            AND cs.charge_type = 'withdraw' AND cs.status = 'transaction_success' " +
            "     GROUP BY date " +
            " ) t7 "+
            " ON t1.date = t7.date ORDER BY t1.date DESC")
    List<FinanceExhibitionDTO> getDailyAmount(@Param("startTime") LocalDateTime startTime, @Param("endTime")LocalDateTime endTime);

    @Select("SELECT t1.date, " +
            "       COALESCE(t2.sum_platform_profit, 0) as sum_platform_profit , " +
            "       COALESCE(t2.sum_fee, 0) as sum_rake , " +
            "       COALESCE(t3.erc20, 0) as fee_erc20 , " +
            "       COALESCE(t3.omni, 0) as fee_omni , " +
            "       COALESCE(t3.charge_miner_eth_fee, 0) as charge_miner_eth_fee , " +
            "       COALESCE(t3.charge_miner_btc_fee, 0) as charge_miner_btc_fee , " +
            "       COALESCE(t4.sum_rebate, 0) as sum_rebate , " +
            "       COALESCE(t5.interest, 0) as interest , " +
            "       COALESCE(t5.rake, 0) as agent_rake , " +
            "       COALESCE(t6.miner_btc_fee, 0) as miner_btc_fee,  " +
            "       COALESCE(t6.miner_eth_fee, 0) as miner_eth_fee  " +
            " FROM( " +
            "      SELECT adddate(date_format(  #{startTime},'%Y-%m-%d'), INTERVAL @num:=@num+1 DAY) as date  " +
            "      FROM `k_line`,(select @num:=-1) t WHERE adddate(date_format(  #{startTime},'%Y-%m-%d'), INTERVAL @num DAY) < date_format(  #{endTime},'%Y-%m-%d')  " +
            "  ) t1  " +
            "  LEFT JOIN ( " +
            "      SELECT date_format(b.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(`platform_profit`), 0) AS sum_platform_profit, " +
            "             ifnull(SUM(`fee`), 0) AS sum_fee " +
            "      FROM `bet` as b " +
            "        WHERE b.create_time <=   #{endTime} AND b.create_time >=   #{startTime} " +
            "      GROUP BY date " +
            "  )t2 ON t1.date = t2.date " +
            "LEFT JOIN(  " +
            "    SELECT date_format(c.create_time, '%Y-%m-%d') as date, " +
            "           ifnull(sum(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END),0) as erc20 , " +
            "           ifnull(sum(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END),0) as omni,  " +
            "           ifnull(SUM(CASE WHEN `miner_fee_type` = 'eth' THEN `miner_fee` ELSE 0 END), 0) AS charge_miner_eth_fee, " +
            "           ifnull(SUM(CASE WHEN `miner_fee_type` = 'btc' THEN `miner_fee` ELSE 0 END), 0) AS charge_miner_btc_fee  " +
            "    FROM charge as c  " +
            "    WHERE c.create_time <=   #{endTime} AND c.create_time >=   #{startTime} " +
            "           AND c.charge_type = 'withdraw' AND c.status = 'chain_success'  " +
            "    GROUP BY date " +
            "  )t3 ON t1.date = t3.date " +
            "    LEFT JOIN ( " +
            "      SELECT date_format(r.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(`rebate_amount`), 0) AS sum_rebate " +
            "      FROM `rebate` as r " +
            "        WHERE r.create_time <=   #{endTime} AND r.create_time >=   #{startTime} " +
            "      GROUP BY date " +
            "  )t4 ON t1.date = t4.date " +
            "    LEFT JOIN ( " +
            "      SELECT date_format(cl.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(CASE WHEN `des` = '利息' THEN `amount` ELSE 0 END ), 0) AS interest, " +
            "             ifnull(SUM(CASE WHEN `des` = '抽水' THEN `amount` ELSE 0 END ), 0) AS rake " +
            "      FROM `currency_log` as cl " +
            "        WHERE cl.create_time <=   #{endTime} AND cl.create_time >=   #{startTime} " +
            "      GROUP BY date " +
            "  )t5 ON t1.date = t5.date" +
            "    LEFT JOIN ( " +
            "      SELECT date_format(ct.complete_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(CASE WHEN ct.`fee_currency_type` = 'btc'  THEN ct.`fee` ELSE 0 END), 0) AS miner_btc_fee, " +
            "             ifnull(SUM(CASE WHEN ct.`fee_currency_type` = 'eth'  THEN ct.`fee` ELSE 0 END), 0) AS miner_eth_fee " +
            "      FROM (SELECT * FROM `chain_tx` WHERE `status`= 'chain_success') ct " +
            "        WHERE ct.complete_time <= #{endTime} AND ct.complete_time >= #{startTime} " +
            "      GROUP BY date " +
            "  )t6 ON t1.date = t6.date ORDER BY t1.date DESC ")
    List<FinanceDailyBreakdownDTO> getDailyBreakdown(@Param("startTime") LocalDateTime startTime, @Param("endTime")LocalDateTime endTime);

    @Select("SELECT t1.date, " +
            "       COALESCE(t2.sum_platform_profit, 0) as sum_platform_profit , " +
            "       COALESCE(t2.sum_fee, 0) as sum_rake , " +
            "       COALESCE(t2.agent_dividends, 0) as agent_dividends , " +
            "       COALESCE(t3.erc20, 0) as fee_erc20 , " +
            "       COALESCE(t3.omni, 0) as fee_omni , " +
            "       COALESCE(t3.charge_miner_eth_fee, 0) as charge_miner_eth_fee , " +
            "       COALESCE(t3.charge_miner_btc_fee, 0) as charge_miner_btc_fee , " +
            "       COALESCE(t4.sum_rebate, 0) as sum_rebate , " +
            "       COALESCE(t5.total, 0) as settled_total , " +
            "       COALESCE(t6.miner_btc_fee, 0) as miner_btc_fee,  " +
            "       COALESCE(t6.miner_eth_fee, 0) as miner_eth_fee  " +
            " FROM( " +
            "      SELECT adddate(date_format(  #{startTime},'%Y-%m-%d'), INTERVAL @num:=@num+1 DAY) as date  " +
            "      FROM `k_line`,(select @num:=-1) t WHERE adddate(date_format(  #{startTime},'%Y-%m-%d'), INTERVAL @num DAY) < date_format(  #{endTime},'%Y-%m-%d')  " +
            "  ) t1  " +
            "  LEFT JOIN ( " +
            "      SELECT date_format(b.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(`platform_profit`), 0) AS sum_platform_profit, " +
            "             ifnull(SUM(`fee`), 0) AS sum_fee, " +
            "             ifnull(SUM(`agent_dividends`),0) agent_dividends " +
            "      FROM `bet` as b " +
            "        WHERE b.create_time <=   #{endTime} AND b.create_time >=   #{startTime} " +
            "      GROUP BY date " +
            "  )t2 ON t1.date = t2.date " +
            "LEFT JOIN(  " +
            "    SELECT date_format(c.create_time, '%Y-%m-%d') as date, " +
            "           ifnull(sum(CASE WHEN `currency_type` = 'usdt_erc20' THEN `fee` ELSE 0 END),0) as erc20 , " +
            "           ifnull(sum(CASE WHEN `currency_type` = 'usdt_omni' THEN `fee` ELSE 0 END),0) as omni,  " +
            "           ifnull(SUM(CASE WHEN `miner_fee_type` = 'eth' THEN `miner_fee` ELSE 0 END), 0) AS charge_miner_eth_fee, " +
            "           ifnull(SUM(CASE WHEN `miner_fee_type` = 'btc' THEN `miner_fee` ELSE 0 END), 0) AS charge_miner_btc_fee  " +
            "    FROM charge as c  " +
            "    WHERE c.create_time <=   #{endTime} AND c.create_time >=   #{startTime} " +
            "           AND c.charge_type = 'withdraw' AND c.status = 'chain_success'  " +
            "    GROUP BY date " +
            "  )t3 ON t1.date = t3.date " +
            "LEFT JOIN ( " +
            "      SELECT date_format(r.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(`rebate_amount`), 0) AS sum_rebate " +
            "      FROM `rebate` as r " +
            "        WHERE r.create_time <=   #{endTime} AND r.create_time >=   #{startTime} " +
            "      GROUP BY date " +
            "  )t4 ON t1.date = t4.date " +
            "LEFT JOIN ( " +
            "      SELECT date_format(cl.create_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(CASE WHEN log_type = 'increase' or log_type = 'increase' THEN -`amount` ELSE `amount` END ), 0) AS total" +
            "      FROM `currency_log` as cl " +
            "        WHERE cl.create_time <= #{endTime} AND cl.create_time >= #{startTime} " +
            "           AND cl.type = 'settlement' AND cl.des = '结算' " +
            "           AND (cl.log_type = 'increase' OR cl.log_type = 'reduce' OR cl.log_type = 'withdraw') " +
            "      GROUP BY date " +
            "  )t5 ON t1.date = t5.date " +
            "LEFT JOIN ( " +
            "      SELECT date_format(ct.complete_time, '%Y-%m-%d') as date, " +
            "             ifnull(SUM(CASE WHEN ct.`fee_currency_type` = 'btc'  THEN ct.`fee` ELSE 0 END), 0) AS miner_btc_fee, " +
            "             ifnull(SUM(CASE WHEN ct.`fee_currency_type` = 'eth'  THEN ct.`fee` ELSE 0 END), 0) AS miner_eth_fee " +
            "      FROM (SELECT * FROM `chain_tx` WHERE `status`= 'chain_success') ct " +
            "        WHERE ct.complete_time <= #{endTime} AND ct.complete_time >= #{startTime} " +
            "      GROUP BY date " +
            "  )t6 ON t1.date = t6.date ORDER BY t1.date DESC ")
    List<FinanceDailyBreakdownDetailsDTO> getDailyBreakdownDetails(@Param("startTime") LocalDateTime startTime, @Param("endTime")LocalDateTime endTime);

    @Select("SELECT  ifnull(COUNT(result = #{result} or null)/COUNT(*), 0) from bet WHERE bet_symbol = #{symbol}")
    double selectProportion(@Param("symbol") String symbol, @Param("result") BetResultEnum result);

    @SelectProvider(type = GenerateSQL.class, method = "betCount")
    int betCount(@Param("id") Long id,
                 @Param("ip") String ip,
                 @Param("equipment") String equipment,
                 @Param("grc_result") Boolean grc_result,
                 @Param("phone") String phone,
                 @Param("result") BetResultEnum result,
                 @Param("startTime") String startTime,
                 @Param("endTime") String endTime,
                 @Param("userIds")  Set<Long> userIds);

    @SelectProvider(type = GenerateSQL.class, method = "betList")
    List<BetPO> betList(@Param("id") Long id,
                        @Param("ip") String ip,
                        @Param("equipment") String equipment,
                        @Param("grc_result") Boolean grc_result,
                        @Param("phone") String phone,
                        @Param("result") BetResultEnum result,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime,
                        @Param("userIds")  Set<Long> userIds,
                        @Param("page") Integer page,
                        @Param("size") Integer size);


    class GenerateSQL {
        public String selectSumProfit(@Param("uid") long uid,
                                      @Param("betType") BetTypeEnum betType,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime,
                                      @Param("result") BetResultEnum result) {
            return mySql("ifnull(SUM(CASE WHEN `result` = 'win' THEN `earn` ELSE 0 END), 0) as totalEarnWin , " +
                    "ifnull(SUM(CASE WHEN `final_BF` = 0 THEN `fee` ELSE 0 END),0) as totalFee, " +
                    "ifnull(SUM(CASE WHEN `result` = 'lose' THEN `earn` ELSE 0 END),0) as totalEarnLose, " +
                    "ifnull(SUM(`final_BF`),0) as totalFeeBF,  ifnull(SUM(`income_BF`),0) as totalIncomeBF ", uid, betType, startTime, endTime, result).toString();
        }

        public String selectSumAmount(@Param("uid") long uid,
                                      @Param("betType") BetTypeEnum betType,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime,
                                      @Param("result") BetResultEnum result) {
            return mySql(" ifnull(SUM(`amount`), 0) ", uid, betType, startTime, endTime, result).toString();
        }

        public String betStatistics(@Param("phone") String phone,
                                    @Param("result") BetResultEnum result,
                                    @Param("startTime") String startTime,
                                    @Param("endTime") String endTime) {
            return mySql(" ifnull(SUM(`amount`), 0) as totalAmount, " +
                    "ifnull(SUM(`earn`), 0) as totalEarn , " +
                    "ifnull(SUM(`income_BF`), 0) as totalIncomeBF , " +
                    "ifnull(SUM(CASE WHEN `final_BF` = 0 THEN `fee` ELSE 0 END),0) as totalFee, " +
                    "ifnull(SUM(`final_BF`),0) as totalFeeBF " , null, null, startTime, endTime, result, phone).toString();
        }

        public String betStatistics2(@Param("ip") String ip,
                                     @Param("equipment") String equipment,
                                     @Param("grc_result") Boolean grc_result,
                                     @Param("uid") Long uid,
                                     @Param("betType") BetTypeEnum betType,
                                     @Param("phone") String phone,
                                     @Param("result") BetResultEnum result,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("userIds")  Set<Long> userIds) {
            return mySql2(" ifnull(SUM(`amount`), 0) as totalAmount, " +
                    "ifnull(SUM(`earn`), 0) as totalEarn , " +
                    "ifnull(SUM(`income_BF`), 0) as totalIncomeBF , " +
                    "ifnull(SUM(CASE WHEN `final_BF` = 0 THEN `fee` ELSE 0 END),0) as totalFee, " +
                    "ifnull(SUM(`final_BF`),0) as totalFeeBF ", null, ip, equipment, grc_result, uid, betType, startTime, endTime, result, phone, userIds,null, null).toString();
        }

        public String betCount(@Param("id") Long id,
                               @Param("ip") String ip,
                               @Param("equipment") String equipment,
                               @Param("grc_result") Boolean grc_result,
                               @Param("phone") String phone,
                               @Param("result") BetResultEnum result,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime,
                               @Param("userIds")  Set<Long> userIds
                               ) {
            return mySql2(" count(*) ", id, ip, equipment, grc_result, null, null, startTime, endTime, result, phone, userIds,null, null).toString();
        }

        public String betList(@Param("id") Long id,
                              @Param("ip") String ip,
                              @Param("equipment") String equipment,
                              @Param("grc_result") Boolean grc_result,
                              @Param("phone") String phone,
                              @Param("result") BetResultEnum result,
                              @Param("startTime") String startTime,
                              @Param("endTime") String endTime,
                              @Param("userIds")  Set<Long> userIds,
                              @Param("page") Integer page,
                              @Param("size") Integer size) {
            return mySql2(" b.id as id, b.create_time as create_time, b.uid as uid, b.uid_username as phone, b.uid_nick as uid_nick, b.uid_avatar as uid_avatar," +
                            " b.bet_type as bet_type, b.bet_time, b.amount, b.discount_amount, b.bet_direction, b.final_direction, b.result,  b.start_exchange_rate as bet_exchange_rate, b.end_exchange_rate as draw_exchange_rate, " +
                            " b.earn, b.fee, b.profit, b.income, b.income_BF, b.base_BF, b.final_BF, b.bet_symbol, b.order_type, " +
                            " uil.grc_score, uil.grc_result, uil.ip, uil.equipment_type, uil.equipment, uil.country, uil.region, uil.city",
                    id, ip, equipment, grc_result, null, null, startTime, endTime, result, phone,userIds, page, size).toString();
        }

        public String selectSumPlatformProfit(@Param("startTime") String startTime,
                                              @Param("endTime") String endTime,
                                              @Param("result") BetResultEnum result,
                                              @Param("phone") String phone) {
            return mySql(" ifnull(SUM(`platform_profit`), 0) ", startTime, endTime, result, phone).toString();
        }

        public String selectSumAgentDividends(@Param("startTime") String startTime,
                                              @Param("endTime") String endTime,
                                              @Param("result") BetResultEnum result,
                                              @Param("phone") String phone) {
            return mySql(" ifnull(SUM(`agent_dividends`), 0) ", startTime, endTime, result, phone).toString();
        }


        public String selectSumAmountByPlatform(@Param("startTime") String startTime,
                                                @Param("endTime") String endTime,
                                                @Param("result") BetResultEnum result,
                                                @Param("phone") String phone) {
            return mySql(" ifnull(SUM(`amount`), 0) ", startTime, endTime, result, phone).toString();
        }

        private SQL mySql(String select,
                          String startTime,
                          String endTime,
                          BetResultEnum result,
                          String phone){
            return mySql(select,null,null,startTime,endTime,result,phone);
        }
        private SQL mySql(String select,
                          Long uid,
                          BetTypeEnum betType,
                          String startTime,
                          String endTime,
                          BetResultEnum result) {
            return mySql(select, uid, betType, startTime, endTime, result, null);
        }
        private SQL mySql(String select,
                          Long uid,
                          BetTypeEnum betType,
                          String startTime,
                          String endTime,
                          BetResultEnum result,
                          String phone) {
            SQL sql = new SQL().SELECT(select).FROM(" `bet` ");
            if(Objects.nonNull(uid)){
                sql.WHERE(" `uid` = #{uid} ");
            }
            if(Objects.nonNull(betType)){
                sql.WHERE(" `bet_type` = #{betType} ");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" `create_time` >= #{startTime} ");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" `create_time` <= #{endTime} ");
            }
            if(Objects.nonNull(result)){
                sql.WHERE(" `result` = #{result} ");
            }
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE(" `uid_username` like  CONCAT('%',#{phone},'%')");
            }
            return sql;
        }

        private SQL mySql2(String select,
                           Long id,
                           String ip,
                           String equipment,
                           Boolean grc_result,
                           Long uid,
                           BetTypeEnum betType,
                           String startTime,
                           String endTime,
                           BetResultEnum result,
                           String phone,
                           Set<Long> userIds,
                           Integer page,
                           Integer size) {
            SQL sql = new SQL().SELECT(select)
                    .FROM(" `bet` b ")
                    .LEFT_OUTER_JOIN(" `user_ip_log` uil ON uil.behavior = '下注' and uil.behavior_id = b.id ")
                    ;
            if (CollUtil.isNotEmpty(userIds)) {
                String uids = ArrayUtil.join(userIds.toArray(), ",");
                sql.WHERE("b.`uid` in ("+uids+")");
            }
            if(Objects.nonNull(id)){
                sql.WHERE(" b.`id` < #{id} ");
            }
            if(Objects.nonNull(uid)){
                sql.WHERE(" b.`uid` = #{uid} ");
            }
            if(Objects.nonNull(betType)){
                sql.WHERE(" b.`bet_type` = #{betType} ");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE(" b.`create_time` >= #{startTime} ");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE(" b.`create_time` <= #{endTime} ");
            }
            if(Objects.nonNull(result)){
                sql.WHERE(" b.`result` = #{result} ");
            }
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE(" b.`uid_username` like  CONCAT('%',#{phone},'%')");
            }
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE(" b.`uid_username` like  CONCAT('%',#{phone},'%') ");
            }
            if(StringUtils.isNotBlank(ip)){
                sql.WHERE(" uil.`ip` like  CONCAT('%',#{ip},'%') ");
            }
            if(StringUtils.isNotBlank(equipment)){
                sql.WHERE(" uil.`equipment` like  CONCAT('%',#{equipment},'%')");
            }
            if(Objects.nonNull(grc_result)){
                sql.WHERE(" uil.`grc_result` = #{grc_result} ");
            }
            if(Objects.nonNull(page) && Objects.nonNull(size)){
                sql.OFFSET(Math.max((page - 1) * size, 0)).LIMIT(size);
            }
            sql.ORDER_BY("b.id desc");
            return sql;
        }
    }
}
