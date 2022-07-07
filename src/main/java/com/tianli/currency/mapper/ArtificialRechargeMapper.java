package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.entity.ArtificialRecharge;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigInteger;

@Mapper
public interface ArtificialRechargeMapper extends BaseMapper<ArtificialRecharge> {

    @SelectProvider(type = GenerateSQL.class, method ="getCount")
    long getCount(@Param("username") String username,
                  @Param("adminNick") String adminNick,
                  @Param("startTime") String startTime,
                  @Param("endTime") String endTime);

    @SelectProvider(type = GenerateSQL.class, method ="getSumAmount")
    BigInteger getSumAmount(@Param("username") String username,
                            @Param("adminNick") String adminNick,
                            @Param("startTime") String startTime,
                            @Param("endTime") String endTime);


    class GenerateSQL{
        private String table =
                "(SELECT cl.id AS id, " +
                "       ui.nick AS nick, " +
                "       ui.username AS username, " +
                "       CASE " +
                "           WHEN des = '人工撤回' THEN (select revoke_admin_nick from artificial_recharge where id = replace(cl.sn, 'ar_', '')) " +
                "           ELSE  (select recharge_admin_nick from artificial_recharge where id = replace(cl.sn, 'ar_', '')) " +
                "           END AS admin_nick, " +
                "       CASE " +
                "           WHEN des = '人工撤回' THEN -amount " +
                "           ELSE  amount " +
                "           END AS amount, " +
                "       cl.create_time as create_time, " +
                "       cl.des " +
                "FROM currency_log cl " +
                "LEFT JOIN user_info ui ON cl.uid = ui.id   " +
                "WHERE cl.type = 'normal' AND cl.des = '人工撤回' OR des = '线下充值') t";

        private SQL pageSQL(String username,
                            String adminNick,
                            String startTime,
                            String endTime,
                            Integer offset,
                            Integer size){
            SQL sql = new SQL()
                    .SELECT("*")
                    .FROM(table);
            addCondition(username, adminNick, startTime, endTime, sql);
            return sql.ORDER_BY("`create_time` DESC").LIMIT("#{offset}, #{size}");
        }

        public String getPage(@Param("username") String username,
                              @Param("adminNick") String adminNick,
                              @Param("startTime") String startTime,
                              @Param("endTime") String endTime,
                              @Param("offset") Integer offset,
                              @Param("size") Integer size){
            return pageSQL(username, adminNick, startTime, endTime, offset, size).toString();
        }

        private SQL sumSQL(String username,
                           String adminNick,
                           String startTime,
                           String endTime){
            SQL sql = new SQL()
                    .SELECT("ifnull(SUM(CASE WHEN `type` = 'recharge' THEN `amount` ELSE  -`amount` END),0)")
                    .FROM("artificial_recharge");
            addCondition(username, adminNick, startTime, endTime, sql);
            return sql;
        }

        public String getSumAmount(@Param("username") String username,
                                   @Param("adminNick") String adminNick,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime){
            return sumSQL(username, adminNick, startTime, endTime).toString();
        }

        private SQL countSQL(String username,
                             String adminNick,
                             String startTime,
                             String endTime){
            SQL sql = new SQL()
                    .SELECT("COUNT(1)")
                    .FROM(table);
            addCondition(username, adminNick, startTime, endTime, sql);
            return sql;
        }

        public String getCount(@Param("username") String username,
                              @Param("adminNick") String adminNick,
                              @Param("startTime") String startTime,
                              @Param("endTime") String endTime){
            return countSQL(username, adminNick, startTime, endTime).toString();
        }

        private void addCondition(String username,
                                 String adminNick,
                                 String startTime,
                                 String endTime,
                                 SQL sql){
            if(StringUtils.isNotBlank(username)){
                sql.WHERE("username LIKE CONCAT('%',#{username},'%')");
            }
            if(StringUtils.isNotBlank(adminNick)){
                sql.WHERE("admin_nike LIKE CONCAT('%',#{adminNike},'%')");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE("`create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE("`create_time` <= #{endTime}");
            }

        }
    }
}

