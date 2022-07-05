package com.tianli.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 用户充值地址表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-14
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    @Select("SELECT * FROM `address` WHERE `btc`=#{btc}")
    Address getByBtc(String to_address);

    @Select("SELECT * FROM `address` WHERE `eth`=#{eth}")
    Address getByEth(String to_address);

    @Select("SELECT * FROM `address` WHERE `eth`= #{from_address} OR `btc` = #{from_address} limit 1")
    Address getByEthBtc(@Param("from_address") String from_address);

    @Select("SELECT * FROM `address` WHERE `tron`=#{tron}")
    Address getByTron(@Param("tron") String tron);

    @Select("SELECT * FROM `address` WHERE `bsc`=#{bsc}")
    Address getByBsc(@Param("bsc") String bsc);
}
