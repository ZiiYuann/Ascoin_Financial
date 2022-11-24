package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.MCoinListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-30
 **/
@Mapper
public interface CoinBaseMapper extends BaseMapper<CoinBase> {

    IPage<MCoinListVO> coins(@Param("page") Page<Coin> page, @Param("query") CoinsQuery query);

    @Update(" UPDATE  coin_base SET show = true WHERE name =#{name}")
    int show(@Param("name") String name);

}
