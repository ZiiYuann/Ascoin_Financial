package com.tianli.management.user.mapper;

import cn.hutool.core.util.StrUtil;
import com.tianli.user.mapper.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Mapper
public interface CustomerManageMapper {

    @SelectProvider(type = GenerateSQL.class, method = "selectCount")
    int selectCount(@Param("phone") String phone,
                    @Param("status") UserStatus status,
                    @Param("user_type") Integer user_type,
                    @Param("startTime") String startTime,
                    @Param("endTime") String endTime,
                    @Param("queryUserIds") String queryUserIds);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumBalance")
    BigInteger selectSumBalance(@Param("phone") String phone,
                                @Param("status") UserStatus status,
                                @Param("user_type") Integer user_type,
                                @Param("startTime") String startTime,
                                @Param("endTime") String endTime,
                                @Param("queryUserIds") String queryUserIds);

    @SelectProvider(type = GenerateSQL.class, method = "newSumBalance")
    BigDecimal newSumBalance(@Param("phone") String phone,
                             @Param("status") UserStatus status,
                             @Param("user_type") Integer user_type,
                             @Param("startTime") String startTime,
                             @Param("endTime") String endTime,
                             @Param("queryUserIds") String queryUserIds);

    @SelectProvider(type = GenerateSQL.class, method = "selectSumBalanceBF")
    BigInteger selectSumBalanceBF(@Param("phone") String phone,
                                  @Param("status") UserStatus status,
                                  @Param("user_type") Integer user_type,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime,
                                  @Param("queryUserIds") String queryUserIds);

    @SelectProvider(type = GenerateSQL.class, method = "selectPage")
    List<CustomerDTO> selectPage(@Param("phone") String phone,
                                 @Param("status") UserStatus status,
                                 @Param("user_type") Integer user_type,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime,
                                 @Param("page") Integer page,
                                 @Param("size") Integer size,
                                 @Param("queryUserIds") String queryUserIds);


    class GenerateSQL {
        public String selectCount(String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
            return sql(" count(*) ", phone, status, user_type, startTime, endTime, queryUserIds, false).toString();
        }

        public String selectSumBalance(String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
            SQL sql = sql("(ifnull(SUM(c.`balance`),0)  + ifnull(SUM(c1.`balance`),0)) as sumBalance", phone, status, user_type, startTime, endTime, queryUserIds, true);
            if (Objects.isNull(user_type)) {
                sql.WHERE("u.user_type = 0");
            }
            return sql.toString();
        }

        public String newSumBalance(String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
            SQL sql = newSql("(ifnull( SUM( c.`balance` ), 0 ) + ifnull( SUM( c1.`balance` ), 0 ) + ifnull( SUM( c2.`balance` ), 0 )) as sumBalance", phone, status, user_type, startTime, endTime, queryUserIds);
            if (Objects.isNull(user_type)) {
                sql.WHERE("u.user_type = 0");
            }
            return sql.toString();
        }

        public String selectSumBalanceBF(String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
            SQL sql = sql("ifnull(SUM(c.`balance_BF`),0) as sumBalance", phone, status, user_type, startTime, endTime, queryUserIds, true);
            if (Objects.isNull(user_type)) {
                sql.WHERE("u.user_type = 0");
            }
            return sql.toString();
        }

        public String selectPage(String phone, UserStatus status, Integer user_type, String startTime, String endTime, Integer page, Integer size, String queryUserIds) {
            String sqlString = sql(" u.`id` as id, u.`username` as phone, ui.`nick` as nick, u.`status` as status, " +
                            "u.`create_time` as create_time, u.`use_robot` as use_robot, u.`auto_count` as auto_count, u.`auto_amount` as auto_amount, " +
                            "u.`interval_time` as interval_time, u.`win_rate` as win_rate, " +
                            "u.`profit_rate` as profit_rate, u.`node` as node, u.`user_type` as user_type, " +
                            "u.`credit_score` as credit_score,"+
                            "u.`adjust_reason` as adjust_reason,"+
                            " a.`eth` as eth, a.`btc` as btc, a.`tron` as trc20, a.`bsc` as bsc, c.`balance` as balance , " +
                            "c.`balance_BF` as balance_BF ,dc.`balance` as weak_balance,uaf.`name` as facebook,ual.`name` as line",
                    phone, status, user_type, startTime, endTime, queryUserIds, true).toString();
            return sqlString + " limit " + Math.max((page - 1) * size, 0) + " , " + size;
        }

        private SQL newSql(String select, String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
            SQL sql = new SQL().SELECT(select)
                    .FROM(" `user` u ")
                    .LEFT_OUTER_JOIN(" `user_info` ui ON ui.`id` = u.`id` ")
                    .LEFT_OUTER_JOIN(" `currency_token` c ON c.`uid` = u.`id`  and c.`type` = 'normal' and (c.token = 'usdt' or c.token = 'usdc')")
                    .LEFT_OUTER_JOIN(" `currency_token` c1 ON c1.`uid` = u.`id` and c1.`type` = 'financial' and (c1.token = 'usdt' or c1.token = 'usdc')")
                    .LEFT_OUTER_JOIN(" `currency_token` c2 ON c2.`uid` = u.`id` and c2.`type` = 'actual' and (c2.token = 'usdt' or c2.token = 'usdc')");
            if (StrUtil.isNotBlank(queryUserIds)) {
                sql.WHERE("u.`id` in (${queryUserIds})");
            }
            if (StringUtils.isNotBlank(phone)) {
                sql.WHERE(" u.`username` like  CONCAT('%',#{phone},'%')  ");
            }
            if (Objects.nonNull(status)) {
                sql.WHERE(" u.`status` = #{status} ");
            }
            if (Objects.nonNull(user_type)) {
                sql.WHERE(" u.`user_type` = #{user_type} ");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" u.`create_time` >= #{startTime} ");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE(" u.`create_time` <= #{endTime} ");
            }
            return sql.ORDER_BY("u.create_time DESC");
        }

        private SQL sql(String select, String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds, boolean isPage) {
            SQL sql = new SQL().SELECT(select)
                    .FROM(" `user` u ");
            if (isPage) {
                sql.LEFT_OUTER_JOIN(" `user_info` ui ON ui.`id` = u.`id` ")
                        .LEFT_OUTER_JOIN(" `currency` c ON c.`uid` = u.`id`  and c.`type` = 'normal' ")
                        .LEFT_OUTER_JOIN(" `currency` c1 ON c1.`uid` = u.`id` and c1.`type` = 'financial' ")
                        .LEFT_OUTER_JOIN(" `address` a ON a.`uid` = u.`id` and a.`type` = 'normal' ")
                        .LEFT_OUTER_JOIN(" `discount_currency` dc ON dc.`id` = u.`id` ")
                        .LEFT_OUTER_JOIN(" `user_authorize` uaf ON uaf.`uid` = u.`id` and uaf.`type` = 'facebook'")
                        .LEFT_OUTER_JOIN(" `user_authorize` ual ON ual.`uid` = u.`id` and ual.`type` = 'line'")
                ;
            }
            if (StrUtil.isNotBlank(queryUserIds)) {
                sql.WHERE("u.`id` in (${queryUserIds})");
            }
            if (StringUtils.isNotBlank(phone)) {
                sql.WHERE(" u.`username` like  CONCAT('%',#{phone},'%')  ");
            }
            if (Objects.nonNull(status)) {
                sql.WHERE(" u.`status` = #{status} ");
            }
            if (Objects.nonNull(user_type)) {
                sql.WHERE(" u.`user_type` = #{user_type} ");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" u.`create_time` >= #{startTime} ");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE(" u.`create_time` <= #{endTime} ");
            }
            return sql.ORDER_BY("u.create_time DESC");
        }
    }
}
