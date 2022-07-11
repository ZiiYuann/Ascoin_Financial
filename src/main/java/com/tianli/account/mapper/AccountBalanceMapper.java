package com.tianli.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountBalance;
import com.tianli.currency.enums.CurrencyAdaptType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 用户余额表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface AccountBalanceMapper extends BaseMapper<AccountBalance> {

    @Select("SELECT * FROM `account_balance` WHERE `uid`=#{uid} AND  `token`=#{token}")
    AccountBalance get(@Param("uid") long uid, @Param("token") CurrencyAdaptType token);

    @Select("SELECT * FROM `account_balance` WHERE `uid`=#{uid} ")
    List<AccountBalance> list(long uid);

    @Update("UPDATE `account_balance` SET `balance`=`balance`+#{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} ")
    long increase(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id}  AND `remain`>= #{amount}")
    long decrease(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance_BF`=`balance_BF`+#{amount},`remain_BF`=`remain_BF`+#{amount} WHERE `uid`=#{id} ")
    long increaseBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`freeze`=`freeze`-#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} ")
    long reduce(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance_BF`=`balance_BF`-#{amount},`freeze_BF`=`freeze_BF`-#{amount} WHERE `uid`=#{id} AND `freeze_BF`>=#{amount} ")
    long reduceBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `remain`>=#{amount} ")
    long withdraw(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance_BF`=`balance_BF`-#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} AND `remain_BF`>=#{amount} ")
    long withdrawBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `freeze`=`freeze`+#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `remain`>=#{amount} ")
    long freeze(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `freeze_BF`=`freeze_BF`+#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} AND `remain_BF`>=#{amount} ")
    long freezeBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `freeze`=`freeze`- #{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} ")
    long unfreeze(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `freeze_BF`=`freeze_BF`-#{amount},`remain_BF`=`remain_BF`+#{amount} WHERE `uid`=#{id} AND `freeze_BF`>=#{amount} ")
    long unfreezeBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Select("SELECT * FROM `account_balance` WHERE `uid` in (${inSql})")
    List<AccountBalance> listByIds(@Param("inSql") String sqlString);

    @Select("SELECT * FROM `account_balance` WHERE `uid` in (${inSql}) and `type` = #{type}")
    List<AccountBalance> listByIdsAndType(@Param("inSql") String sqlString);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id}  ")
    long lowWithdraw(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance_BF`=`balance_BF`-#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id}  ")
    long lowWithdrawBF(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Select("SELECT cd.uid " +
            "FROM (select * FROM account_balance WHERE type = 'deposit') cd " +
            "LEFT JOIN account_balance cs ON cd.uid = cs.uid AND cs.type = 'settlement' " +
            "WHERE cs.balance < 0 AND ABS(cs.balance)/ABS(cd.balance)>0.8")
    List<Long> listAgentFocus();

    @Update("UPDATE `account_balance` SET `balance`=`balance`- #{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} ")
    long withdrawPresumptuous(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE `account_balance` SET `balance_BF`=`balance_BF`- #{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} ")
    long withdrawPresumptuousBF(@Param("id") long id ,@Param("amount") BigDecimal amount);

}
