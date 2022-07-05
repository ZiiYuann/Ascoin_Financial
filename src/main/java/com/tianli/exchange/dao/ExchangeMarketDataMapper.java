package com.tianli.exchange.dao;

import com.tianli.exchange.entity.ExchangeMarketData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 成交记录表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-06-16
 */
@Mapper
public interface ExchangeMarketDataMapper extends BaseMapper<ExchangeMarketData> {

}
