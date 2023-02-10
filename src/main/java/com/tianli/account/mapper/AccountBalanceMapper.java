package com.tianli.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.vo.AccountBalanceSimpleVO;
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

    @Select("SELECT * FROM `account_balance` WHERE `uid`=#{uid} AND  `coin`=#{coin}")
    AccountBalance get(@Param("uid") long uid, @Param("coin") String coin);

    @Select("SELECT * FROM `account_balance` WHERE `uid`=#{uid} ")
    List<AccountBalance> list(long uid);

    @Update("UPDATE `account_balance` SET `balance`=`balance`+#{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} and coin=#{coin}")
    long increase(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id}  and coin=#{coin} AND `remain`>= #{amount}")
    long decrease(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`freeze`=`freeze`-#{amount} WHERE `uid`=#{id} and coin=#{coin} AND  `freeze`>=#{amount} ")
    long reduce(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `balance`=`balance`-#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} and coin=#{coin} AND `remain`>=#{amount} ")
    long withdraw(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `freeze`=`freeze`+#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} and coin=#{coin} AND `remain`>=#{amount} ")
    long freeze(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `freeze`=`freeze`- #{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} and coin=#{coin} AND `freeze`>=#{amount} ")
    long unfreeze(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Select("select sum(balance) as balanceAmount ,coin from account_balance GROUP BY coin")
    List<AccountBalanceSimpleVO> listAccountBalanceSimpleVO();

    @Update("UPDATE `account_balance` SET `pledge_freeze`=`pledge_freeze`+#{amount},`remain`=`remain`-#{amount} WHERE `uid`=#{id} and coin=#{coin} AND `remain`>=#{amount} ")
    long pledgeFreeze(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);

    @Update("UPDATE `account_balance` SET `pledge_freeze`=`pledge_freeze`- #{amount},`remain`=`remain`+#{amount} WHERE `uid`=#{id} and coin=#{coin} AND `pledge_freeze`>=#{amount} ")
    long pledgeUnfreeze(@Param("id") long id, @Param("amount") BigDecimal amount, @Param("coin") String coin);
}
