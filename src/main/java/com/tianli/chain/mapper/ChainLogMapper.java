package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.chain.entity.ChainLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author wangqiyun
 * @since 2020/11/14 18:04
 */

@Mapper
public interface ChainLogMapper extends BaseMapper<ChainLog> {
    @Insert("REPLACE INTO `chain_log`(`id`, `address`, `currency_type`, `amount`, `uid`, `username`, `u_create_time`) VALUES (#{id},#{address},#{currency_type},#{amount},#{uid},#{username},#{u_create_time})")
    long replace(ChainLog chainLog);

    @Select("SELECT IFNULL(MAX(`id`), 0) FROM `chain_log`")
    long maxId();
}
