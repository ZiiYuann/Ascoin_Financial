package com.tianli.currency.controller;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



    @GetMapping("coin")
    public Result coin(){
        CurrencyCoin[] values = CurrencyCoin.values();
        List<String> coins = Arrays.stream(values).map(CurrencyCoin::getName).collect(Collectors.toList());

        Map<String,List<String>> result = new HashMap<>();
        result.put("coins",coins);
        return Result.success().setData(result);
    }
}
