package com.tianli.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

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



    @Select("SELECT * FROM `address` WHERE `eth`=#{toAddress}")
    Address getByEth(@Param("toAddress") String toAddress);

    @Select("SELECT * FROM `address` WHERE `tron`=#{tron}")
    Address getByTron(@Param("tron") String tron);

    @Select("SELECT * FROM `address` WHERE `bsc`=#{bsc}")
    Address getByBsc(@Param("bsc") String bsc);

    @Select("SELECT max(id) FROM `address` ")
    Long maxId();

    @Select("SELECT * FROM `address` WHERE id <= ${id}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 500)
    @ResultType(Address.class)
    void flow(@Param("id") Long id, ResultHandler<Address> handler);
}
