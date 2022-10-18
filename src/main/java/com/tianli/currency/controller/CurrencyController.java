package com.tianli.currency.controller;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@RestController
@RequestMapping("/currency")
public class CurrencyController {

    @Resource
    private CurrencyService currencyService;

    @GetMapping("coin")
    public Result coin(){
        CurrencyCoin[] values = CurrencyCoin.values();
        List<String> coins = Arrays.stream(values).map(CurrencyCoin::getName).collect(Collectors.toList());

        Map<String,List<String>> result = new HashMap<>();
        result.put("coins",coins);
        return Result.success().setData(result);
    }

    @GetMapping("/rate/{coin}")
    public Result rate(@PathVariable CurrencyCoin coin) {
        BigDecimal dollarRate = currencyService.getDollarRate(coin);
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("rate", dollarRate.toPlainString());
        return Result.success().setData(rateMap);
    }
}
