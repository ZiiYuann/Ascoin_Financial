package com.tianli.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountBalanceOperationLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 余额变动记录表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface AccountBalanceOperationLogMapper extends BaseMapper<AccountBalanceOperationLog> {

    @Select("select ifnull(sum(`amount`), 0) from `account_balance_operation` where `uid` = #{uid} and `create_time` between #{startTime} and #{endTime} and `des` = '抽水' ")
    BigInteger selectTotalRebateAmountWithInterval(@Param("uid") long uid, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("select ifnull(sum(`amount`), 0) from `account_balance_operation` where `uid` = #{uid} and `des` = '抽水' ")
    BigInteger selectTotalRebateAmount(@Param("uid") long uid);

    @SelectProvider(type = GenerateSQL.class, method = "countRake")
    long rakeRecordCount(@Param("uid") Long uid,
                         @Param("phone") String phone,
                         @Param("bet_id") String bet_id,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);

    @Select("select ifnull(sum(`amount`), 0) from `account_balance_operation` where `uid` = #{uid} and `des` = '利息'")
    BigInteger selectSumMiningAmount(Long uid);

    class GenerateSQL{
        private SQL countSQL(Long uid, String phone, String bet_id, String startTime, String endTime){
            SQL sql = new SQL().SELECT("count(1)")
                    .FROM("(SELECT * FROM account_balance_operation WHERE uid = #{uid} AND des = '抽水') cl")
                    .LEFT_OUTER_JOIN("`bet` AS b ON replace(cl.sn,'rake_','') = b.id");
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE("b.`uid_username` like CONCAT('%',#{phone},'%')");
            }
            if(StringUtils.isNotBlank(bet_id)){
                sql.WHERE("b.`id` like CONCAT('%',#{bet_id},'%')");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE("cl.`create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE("cl.`create_time` <= #{endTime}");
            }
            return sql;
        }

        public String countRake(@Param("uid") Long uid,
                                @Param("phone") String phone,
                                @Param("bet_id") String bet_id,
                                @Param("startTime") String startTime,
                                @Param("endTime") String endTime){
            return countSQL(uid, phone, bet_id, startTime, endTime).toString();
        }

        private SQL selectSQL(Long uid, String phone, String bet_id, String startTime, String endTime, Integer offset, Integer size){
            SQL sql = new SQL().SELECT("cl.`id` as id, b.uid_username as username, b.uid_nick as nick, b.uid as uid, " +
                                        "b.`id` as bet_id, b.amount as amount, cl.amount as rake , cl.create_time as create_time")
                    .FROM("(SELECT * FROM account_balance_operation WHERE uid = #{uid} AND des = '抽水') cl")
                    .LEFT_OUTER_JOIN("`bet` AS b ON replace(cl.sn,'rake_','') = b.id");
            if(StringUtils.isNotBlank(phone)){
                sql.WHERE("b.`uid_username` like CONCAT('%',#{phone},'%')");
            }
            if(StringUtils.isNotBlank(bet_id)){
                sql.WHERE("b.`id` like CONCAT('%',#{bet_id},'%')");
            }
            if(StringUtils.isNotBlank(startTime)){
                sql.WHERE("cl.`create_time` >= #{startTime}");
            }
            if(StringUtils.isNotBlank(endTime)){
                sql.WHERE("cl.`create_time` <= #{endTime}");
            }
            return sql.ORDER_BY("cl.`create_time` DESC").LIMIT("#{offset}, #{size}");
        }

        public String rakeRecord(@Param("uid") Long uid,
                                 @Param("phone") String phone,
                                 @Param("bet_id") String bet_id,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime,
                                 @Param("offset") Integer offset,
                                 @Param("size") Integer size){
            return selectSQL(uid, phone, bet_id, startTime, endTime, offset, size).toString();
        }
    }
}
