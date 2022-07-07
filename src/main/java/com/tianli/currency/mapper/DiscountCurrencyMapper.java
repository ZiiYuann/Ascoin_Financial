package com.tianli.currency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;

/**
 * <p>
 * 用户余额表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface DiscountCurrencyMapper extends BaseMapper<AccountBalance> {

    @Update("UPDATE `discount_currency` SET `balance`=`balance`-#{amount} WHERE `id`=#{uid} AND `balance`>= #{amount}")
    int withdraw(@Param("uid") long uid, @Param("amount") BigInteger amount);

    @Update("UPDATE `discount_currency` SET `balance`=`balance`+ #{amount} WHERE `id`=#{uid}")
    int increase(@Param("uid") Long uid, @Param("amount") BigInteger amount);

    @Update("UPDATE `discount_currency` SET `balance`=`balance`+ #{amount}, `new_gift` = 1 WHERE `id`=#{uid} and `new_gift` = 0")
    int increaseNew(@Param("uid") Long uid, @Param("amount") BigInteger amount);
}
