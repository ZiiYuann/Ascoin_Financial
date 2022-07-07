package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.charge.ChargeType;
import com.tianli.charge.dto.StatChargeAmount;
import com.tianli.currency.TokenCurrencyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:25
 */
@Mapper
public interface ChargeMapper extends BaseMapper<Charge> {
    @Insert("INSERT INTO `charge`(`id`, `create_time`, `complete_time`, `status`, `uid`, `uid_nick`, `uid_username`, `uid_avatar`, `sn`, `currency_type`, `charge_type`, `amount`, `fee`, `real_amount`, `from_address`, `to_address`, `txid`, `note`) VALUES (#{id},#{create_time},#{complete_time},#{status},#{uid}, #{uid_nick}, #{uid_username}, #{uid_avatar},#{sn},#{currency_type},#{charge_type},#{amount},#{fee},#{real_amount},#{from_address},#{to_address},#{txid},#{note})")
    long insertCharge(Charge charge);

    @Select("SELECT * FROM `charge` WHERE `sn`=#{sn}")
    Charge getBySn(String sn);

    @Select("SELECT * FROM `charge` WHERE `id`=#{id}")
    Charge getById(long id);

    @Update("UPDATE `charge` SET `complete_time`=#{complete_time},`status`=#{success} ,`miner_fee`=#{miner_fee},`miner_fee_type` = #{miner_fee_type} WHERE `id`=#{id} AND `status`=#{created}")
    long success(@Param("complete_time") LocalDateTime complete_time, @Param("success") ChargeStatus success,
                 @Param("id") Long id, @Param("created") ChargeStatus created,
                 @Param("miner_fee") BigInteger miner_fee, @Param("miner_fee_type") TokenCurrencyType miner_fee_type);

    @Update("UPDATE `charge` SET `complete_time`=#{complete_time},`status`=#{fail} ,`miner_fee`=#{miner_fee},`miner_fee_type` = #{miner_fee_type} WHERE `id`=#{id} AND `status`=#{chaining}")
    long fail(@Param("complete_time") LocalDateTime complete_time, @Param("fail") ChargeStatus fail,
              @Param("id") long id, @Param("chaining") ChargeStatus chaining,
              @Param("miner_fee") BigInteger miner_fee, @Param("miner_fee_type") TokenCurrencyType miner_fee_type);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumChargeAmount")
    Map<String, BigDecimal> selectSumChargeAmount(@Param("type") ChargeType type, @Param("phone") String phone, @Param("txid") String txid, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @SelectProvider(type = GenerateSQL.class, method = "selectPage")
    List<Charge> selectPage(@Param("uid") Long uid,
                            @Param("status") ChargeStatus status,
                            @Param("type") ChargeType type,
                            @Param("phone") String phone,
                            @Param("txid") String txid,
                            @Param("startTime") String startTime,
                            @Param("endTime") String endTime,
                            @Param("offset") int offset,
                            @Param("size") int size);


    @SelectProvider(type = GenerateSQL.class, method = "totalAmount")
    List<Map<String,Object>> totalAmount(@Param("ip") String ip,
                                         @Param("equipment") String equipment,
                                         @Param("grc_result") Boolean grc_result,
                                         @Param("otherSec") Boolean otherSec,
                                         @Param("uid") Long uid,
                                         @Param("status") ChargeStatus status,
                                         @Param("type") ChargeType type,
                                         @Param("phone") String phone,
                                         @Param("txid") String txid,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime);


    @SelectProvider(type = GenerateSQL.class, method = "selectCount")
    int selectCount(@Param("uid") Long uid,
                    @Param("status") ChargeStatus status,
                    @Param("type") ChargeType type,
                    @Param("phone") String phone,
                    @Param("txid") String txid,
                    @Param("startTime") String startTime,
                    @Param("endTime") String endTime);


    @Select("SELECT `currency_type`, SUM(`amount`) as total_amount FROM `charge` WHERE `status` = 'chain_success' AND `charge_type` = 'withdraw' GROUP BY `currency_type`")
    List<StatChargeAmount> totalWithdrawAmount();


    @SelectProvider(type = GenerateSQL.class, method = "selectNewCount")
    int selectNewCount(@Param("ip") String ip,
                       @Param("equipment") String equipment,
                       @Param("grc_result") Boolean grc_result,
                       @Param("otherSec") Boolean otherSec,
                       @Param("uid") Long uid,
                       @Param("status") ChargeStatus status,
                       @Param("type") ChargeType type,
                       @Param("phone") String phone,
                       @Param("txid") String txid,
                       @Param("startTime") String startTime,
                       @Param("endTime") String endTime);

    @Select("SELECT ifnull(SUM(`amount`), 0) FROM `charge` WHERE `uid` = #{uid} and `charge_type` = #{chargeType} and `currency_type` = #{currency_type} and status in ('created','chaining','chain_success') ")
    BigDecimal getTotalAmount(@Param("uid") Long uid, @Param("chargeType") ChargeType chargeType, @Param("currency_type") TokenCurrencyType currency_type);


    class GenerateSQL {
        public String selectPage(Long uid,
                                 ChargeStatus status,
                                 ChargeType type,
                                 String phone,
                                 String txid,
                                 String startTime,
                                 String endTime,
                                 int offset,
                                 int size) {
            SQL sql = sql(" * ", status, type, uid, phone, txid, startTime, endTime);
            sql.ORDER_BY(" `id` desc ");
            return sql.toString() + " limit " + offset + " , " + size;
        }

        public String totalAmount(String ip,
                                  String equipment,
                                  Boolean grc_result,
                                  Boolean otherSec,
                                  Long uid,
                                  ChargeStatus status,
                                  ChargeType type,
                                  String phone,
                                  String txid,
                                  String startTime,
                                  String endTime) {
            SQL sql = sqlNew(ip, equipment, grc_result, otherSec, "c.token,sum(c.amount) as sum",
                    status, type, uid, phone, txid, startTime, endTime);
            sql.WHERE("u.user_type = 0");
            sql.GROUP_BY("c.token");
            return sql.toString();
        }

        public String selectNewPage(String ip,
                                    String equipment,
                                    Boolean grc_result,
                                    Boolean otherSec,
                                    Long uid,
                                    ChargeStatus status,
                                    ChargeType type,
                                    String phone,
                                    String txid,
                                    String startTime,
                                    String endTime,
                                    int offset,
                                    int size) {
            SQL sql = sqlNew(ip, equipment, grc_result, otherSec, " c.id, c.create_time, c.uid_username, c.uid_nick, c.status, c.uid, c.sn, c.token, c.currency_type, c.amount, c.fee, c.real_amount, c.from_address, c.to_address, c.txid, c.note, " +
                            "c.reviewer,c.reviewer_time,c.reason,c.reason_en,c.review_note," +
                            "uil.grc_score, uil.grc_result, uil.ip, uil.equipment_type, uil.equipment, uil.country, uil.region, uil.city,u.user_type",
                    status, type, uid, phone, txid, startTime, endTime);
            sql.ORDER_BY(" c.`id` desc ");
            return sql.toString() + " limit " + offset + " , " + size;
        }

        public String selectCount(Long uid,
                                  ChargeStatus status,
                                  ChargeType type,
                                  String phone,
                                  String txid,
                                  String startTime,
                                  String endTime) {
            SQL sql = sql(" count(*) ", status, type, uid, phone, txid, startTime, endTime);
            return sql.toString();
        }

        public String selectNewCount(String ip,
                                     String equipment,
                                     Boolean grc_result,
                                     Boolean otherSec,
                                     Long uid,
                                     ChargeStatus status,
                                     ChargeType type,
                                     String phone,
                                     String txid,
                                     String startTime,
                                     String endTime) {
            SQL sql = sqlNew(ip, equipment, grc_result, otherSec, " count(*) ", status, type, uid, phone, txid, startTime, endTime);
            return sql.toString();
        }


        public String selectSumChargeAmount(ChargeType type,
                                            String phone,
                                            String txid,
                                            String startTime,
                                            String endTime) {
            SQL sql = new SQL().SELECT(" SUM(CASE WHEN `currency_type` = 'usdt_erc20' THEN `amount` ELSE 0 END) AS sumAmountErc20 , " +
                    "SUM(CASE WHEN `currency_type` = 'usdt_bep20' THEN `amount` ELSE 0 END) AS sumAmountBep20 , " +
                    "SUM(CASE WHEN `currency_type` = 'usdt_trc20' THEN `amount` ELSE 0 END) AS sumAmountTrc20 , " +
                    "SUM(CASE WHEN `currency_type` = 'usdc_erc20' THEN `amount` ELSE 0 END) AS sumAmountUsdcErc20 , " +
                    "SUM(CASE WHEN `currency_type` = 'usdc_bep20' THEN `amount` ELSE 0 END) AS sumAmountUsdcBep20 , " +
                    "SUM(CASE WHEN `currency_type` = 'usdc_trc20' THEN `amount` ELSE 0 END) AS sumAmountUsdcTrc20 , " +
                    "SUM(CASE WHEN `currency_type` = 'BF_bep20' THEN `amount` ELSE 0 END) AS sumAmountBF ")
                    .FROM(" `charge` ")
                    .WHERE(" `charge_type`=#{type} ");
            if (StringUtils.isNotBlank(phone)) {
                sql.WHERE(" `uid_username`  like CONCAT('%',#{phone},'%')");
            }
            if (StringUtils.isNotBlank(txid)) {
                sql.WHERE(" `txid`  like CONCAT('%',#{txid},'%') ");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" `create_time` >= #{startTime} ");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE("  `create_time` <= #{endTime} ");
            }
            return sql.toString();
        }

        private SQL sql(String select,
                        ChargeStatus status,
                        ChargeType type,
                        Long uid,
                        String phone,
                        String txid,
                        String startTime,
                        String endTime) {
            SQL sql = new SQL().SELECT(select).FROM(" `charge` ");
            if (Objects.nonNull(uid)) {
                sql.WHERE(" `uid` = #{uid} ");
            }
            if (Objects.nonNull(status)) {
                sql.WHERE(" `status` = #{status} ");
            }
            if (Objects.nonNull(type)) {
                sql.WHERE(" `charge_type` = #{type} ");
            }
            if (StringUtils.isNotBlank(phone)) {
                sql.WHERE(" `uid_username` like CONCAT('%',#{phone},'%') ");
            }
            if (StringUtils.isNotBlank(txid)) {
                sql.WHERE(" `txid` like CONCAT('%',#{txid},'%') ");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" `create_time` >= #{startTime} ");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE(" `create_time` <= #{endTime} ");
            }
            return sql;
        }

        private SQL sqlNew(String ip,
                           String equipment,
                           Boolean grc_result,
                           Boolean otherSec,
                           String select,
                           ChargeStatus status,
                           ChargeType type,
                           Long uid,
                           String phone,
                           String txid,
                           String startTime,
                           String endTime) {
            SQL sql = new SQL().SELECT(select)
                    .FROM(" `charge` c ")
                    .LEFT_OUTER_JOIN(" `user_ip_log` uil on uil.behavior_id = c.id and uil.behavior = '提现' ")
                    .LEFT_OUTER_JOIN("`user` u on u.id = c.uid");
            if (Objects.nonNull(otherSec) && otherSec) {
                if (StringUtils.isNotBlank(ip)) {
                    sql.WHERE(" uil.`ip` = #{ip} ");
                }
                if (StringUtils.isNotBlank(equipment)) {
                    sql.WHERE(" uil.`equipment` = #{equipment} ");
                }
                if (StringUtils.isNotBlank(phone)) {
                    if (StringUtils.isNotBlank(equipment) || StringUtils.isNotBlank(ip)) {
                        sql.WHERE(" c.`uid_username` != #{phone} ");
                    } else {
                        sql.WHERE(" c.`uid_username` = #{phone} ");
                    }
                }
            } else {
                if (StringUtils.isNotBlank(ip)) {
                    sql.WHERE(" uil.`ip` like CONCAT('%',#{ip},'%') ");
                }
                if (StringUtils.isNotBlank(equipment)) {
                    sql.WHERE(" uil.`equipment` like CONCAT('%',#{equipment},'%') ");
                }
                if (StringUtils.isNotBlank(phone)) {
                    sql.WHERE(" c.`uid_username` like CONCAT('%',#{phone},'%') ");
                }
            }
            if (Objects.nonNull(grc_result)) {
                sql.WHERE(" uil.`grc_result` = #{grc_result} ");
            }
            if (Objects.nonNull(grc_result)) {
                sql.WHERE(" uil.`grc_result` = #{grc_result} ");
            }
            if (Objects.nonNull(uid)) {
                sql.WHERE(" c.`uid` = #{uid} ");
            }
            if (Objects.nonNull(status)) {
                sql.WHERE(" c.`status` = #{status} ");
            }
            if (Objects.nonNull(type)) {
                sql.WHERE(" c.`charge_type` = #{type} ");
            }

            if (StringUtils.isNotBlank(txid)) {
                sql.WHERE(" c.`txid` like CONCAT('%',#{txid},'%') ");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" c.`create_time` >= #{startTime} ");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE(" c.`create_time` <= #{endTime} ");
            }
            return sql;
        }
    }
}
