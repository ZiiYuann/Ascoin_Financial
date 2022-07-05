package com.tianli.exchange.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exchange.dao.ExchangeMarketDataMapper;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.entity.ExchangeMarketData;
import com.tianli.exchange.push.TradeStream;
import com.tianli.exchange.service.IExchangeMarketDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 成交记录表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-06-16
 */
@Service
public class ExchangeMarketDataServiceImpl extends ServiceImpl<ExchangeMarketDataMapper, ExchangeMarketData> implements IExchangeMarketDataService {

    @Override
    public ExchangeMarketData add(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        ExchangeMarketData marketData = new ExchangeMarketData();
        marketData.setId(CommonFunction.generalId());
        marketData.setBuy_order_id(coreResponseDTO.getBuy_id());
        marketData.setSell_order_id(coreResponseDTO.getSell_id());
        marketData.setPrice(coreResponseDTO.getPrice());
        marketData.setVolume(coreResponseDTO.getQuantity());
        marketData.setTransaction_time(coreResponseDTO.getTime());
        marketData.setSymbol(coreResponseDTO.getSymbol());
        marketData.setToken_stock(CurrencyCoinEnum.getTokenStock(marketData.getSymbol()).getName());
        marketData.setToken_fiat(CurrencyCoinEnum.usdt.name());
        marketData.setCreate_time(LocalDateTime.now());
        this.save(marketData);
        return marketData;
    }

    @Override
    public List<TradeStream> trades(String symbol, Integer limit) {
        List<ExchangeMarketData> exchangeMarketDataList = this.list(Wrappers.lambdaQuery(ExchangeMarketData.class)
                .eq(ExchangeMarketData::getSymbol, symbol)
                .orderByDesc(ExchangeMarketData::getId)
                .last("limit" + " " + limit));
        List<TradeStream> tradeStreams = new ArrayList<>();
        if (CollUtil.isNotEmpty(exchangeMarketDataList)) {
            ListUtil.reverse(exchangeMarketDataList);
            long time = System.currentTimeMillis();
            exchangeMarketDataList.forEach(exchangeMarketData -> tradeStreams.add(TradeStream.getTradeStream(exchangeMarketData, time)));
        }
        return tradeStreams;
    }
}
