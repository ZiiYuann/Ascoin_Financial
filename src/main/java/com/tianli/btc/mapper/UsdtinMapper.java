package com.tianli.btc.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author cs
 * @Date 2022-01-18 9:44 上午
 */
@Mapper
public interface UsdtinMapper {
    @Insert("REPLACE INTO `usdt_in`(`txid`, `sendingaddress`, `referenceaddress`, `amount`, `block`, `fee`, `create_time`, `valid`) VALUES (#{txid},#{sendingaddress},#{referenceaddress},#{amount},#{block},#{fee},#{create_time},#{valid})")
    long insert(Usdtin usdtin);
}
