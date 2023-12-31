package com.tianli.currency.controller;

import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.vo.CoinBaseVO;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Resource
    private CoinBaseService coinBaseService;

    @GetMapping("coin")
    public Result coin() {
        Set<String> coins = coinBaseService.pushCoinNames();

        Map<String, Set<String>> result = new HashMap<>();
        result.put("coins", coins);
        return Result.success().setData(result);
    }

    @GetMapping("coin/infos")
    public Result<List<CoinBaseVO>> coinInfos() {
        var coins = coinBaseService.getPushListCache();
        var result = coins.stream().map(coinBase -> new CoinBaseVO(coinBase.getName(), coinBase.getLogo()))
                .collect(Collectors.toList());
        return new Result<>(result);
    }

    @GetMapping("/rate/{coin}")
    public Result rate(@PathVariable String coin) {
        BigDecimal dollarRate = currencyService.getDollarRate(coin);
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("rate", dollarRate.toPlainString());
        return Result.success().setData(rateMap);
    }
}
