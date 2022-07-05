package com.tianli.exchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.entity.ExchangeMarketData;
import com.tianli.exchange.push.TradeStream;

import java.util.List;

/**
 * <p>
 * 成交记录表 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-06-16
 */
public interface IExchangeMarketDataService extends IService<ExchangeMarketData> {
    /**
     * 添加成交记录
     * @param coreResponseDTO
     */
    ExchangeMarketData add(ExchangeCoreResponseConvertDTO coreResponseDTO);

    /**
     * 根据交易对查询成交列表
     * @param symbol
     * @param limit
     * @return
     */
    List<TradeStream> trades(String symbol, Integer limit);
}
