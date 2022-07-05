package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.CurrencyTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
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
public interface CurrencyMapper extends BaseMapper<Currency> {

    @Select("SELECT * FROM `currency` WHERE `uid`=#{uid} AND `type`=#{type}")
    Currency get(@Param("uid") long uid, @Param("type") CurrencyTypeEnum type);

    @Select("SELECT * FROM `currency` WHERE `uid`=#{uid}")
    List<Currency> list(long uid);

    @Update("UPDATE `currency` SET `balance`=`balance`+#{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} AND `type`=#{type}")
    long increase(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `type`=#{type} AND `remain`>= #{amount}")
    long decrease(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance_BF`=`balance_BF`+#{amount},`remain_BF`=`remain_BF`+#{amount} WHERE `uid`=#{id} AND `type`=#{type}")
    long increaseBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance`=`balance`-#{amount},`freeze`=`freeze`-#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} AND `type`=#{type}")
    long reduce(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance_BF`=`balance_BF`-#{amount},`freeze_BF`=`freeze_BF`-#{amount} WHERE `uid`=#{id} AND `freeze_BF`>=#{amount} AND `type`=#{type}")
    long reduceBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `remain`>=#{amount} AND `type`=#{type}")
    long withdraw(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance_BF`=`balance_BF`-#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} AND `remain_BF`>=#{amount} AND `type`=#{type}")
    long withdrawBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `freeze`=`freeze`+#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `remain`>=#{amount} AND `type`=#{type}")
    long freeze(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `freeze_BF`=`freeze_BF`+#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} AND `remain_BF`>=#{amount} AND `type`=#{type}")
    long freezeBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `freeze`=`freeze`- #{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} AND `type`=#{type}")
    long unfreeze(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `freeze_BF`=`freeze_BF`-#{amount},`remain_BF`=`remain_BF`+#{amount} WHERE `uid`=#{id} AND `freeze_BF`>=#{amount} AND `type`=#{type}")
    long unfreezeBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Select("SELECT * FROM `currency` WHERE `uid` in (${inSql})")
    List<Currency> listByIds(@Param("inSql") String sqlString);

    @Select("SELECT * FROM `currency` WHERE `uid` in (${inSql}) and `type` = #{type}")
    List<Currency> listByIdsAndType(@Param("inSql") String sqlString, @Param("type") CurrencyTypeEnum type);

    @Update("UPDATE `currency` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id}  AND `type`=#{type}")
    long lowWithdraw(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance_BF`=`balance_BF`-#{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id}  AND `type`=#{type}")
    long lowWithdrawBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Select("SELECT cd.uid " +
            "FROM (select * FROM currency WHERE type = 'deposit') cd " +
            "LEFT JOIN currency cs ON cd.uid = cs.uid AND cs.type = 'settlement' " +
            "WHERE cs.balance < 0 AND ABS(cs.balance)/ABS(cd.balance)>0.8")
    List<Long> listAgentFocus();

    @Update("UPDATE `currency` SET `balance`=`balance`- #{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `type`=#{type}")
    long withdrawPresumptuous(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

    @Update("UPDATE `currency` SET `balance_BF`=`balance_BF`- #{amount},`remain_BF`=`remain_BF`-#{amount} WHERE `uid`=#{id} AND `type`=#{type}")
    long withdrawPresumptuousBF(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("amount") BigInteger amount);

}
