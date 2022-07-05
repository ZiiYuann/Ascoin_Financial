package com.tianli.currency_token.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.CurrencyTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface CurrencyTokenMapper extends BaseMapper<CurrencyToken> {

    @Select("SELECT * FROM `currency_token` WHERE `uid`=#{uid} AND `type`=#{type} AND `token` = #{token}")
    CurrencyToken get(@Param("uid") long uid, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token);

    @Update("UPDATE `currency_token` SET `balance`=`balance`+#{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} AND `type`=#{type} AND `token` = #{token}")
    long increase(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token, @Param("amount") BigDecimal amount);

    @Update("UPDATE `currency_token` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `type`=#{type} AND `remain`>= #{amount} AND `token` = #{token}")
    long decrease(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token, @Param("amount") BigDecimal amount);

    @Update("UPDATE `currency_token` SET `freeze`=`freeze`+#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} AND `remain`>=#{amount} AND `type`=#{type} AND `token` = #{token}")
    long freeze(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token, @Param("amount") BigDecimal amount);

    @Update("UPDATE `currency_token` SET `balance`=`balance`-#{amount},`freeze`=`freeze`-#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} AND `type`=#{type} AND `token` = #{token}")
    long reduce(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token, @Param("amount") BigDecimal amount);

    @Update("UPDATE `currency_token` SET `freeze`=`freeze`- #{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} AND `freeze`>=#{amount} AND `type`=#{type} AND `token` = #{token}")
    long unfreeze(@Param("id") long id, @Param("type") CurrencyTypeEnum type, @Param("token") CurrencyCoinEnum token, @Param("amount") BigDecimal amount);

}
