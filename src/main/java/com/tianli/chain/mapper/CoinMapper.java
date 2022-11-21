package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.chain.entity.Coin;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
@Mapper
public interface CoinMapper extends BaseMapper<Coin> {
}
