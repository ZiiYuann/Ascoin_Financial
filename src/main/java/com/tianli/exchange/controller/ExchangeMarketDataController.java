package com.tianli.exchange.controller;


import com.tianli.exception.Result;
import com.tianli.exchange.push.TradeStream;
import com.tianli.exchange.service.IExchangeMarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 成交记录表 前端控制器
 * </p>
 *
 * @author lzy
 * @since 2022-06-16
 */
@RestController
@RequestMapping("/exchangeMarketData")
public class ExchangeMarketDataController {

    @Resource
    IExchangeMarketDataService exchangeMarketDataService;

    @GetMapping("/trades")
    public Result trades(String symbol, @RequestParam(required = false, defaultValue = "500") Integer limit) {
        List<TradeStream> exchangeMarketDataList = exchangeMarketDataService.trades(symbol, limit);
        return Result.success(exchangeMarketDataList);
    }

}

