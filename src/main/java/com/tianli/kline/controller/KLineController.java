package com.tianli.kline.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.currency.DigitalCurrencyExchangeComponent;
import com.tianli.currency.NationalCurrencyEnum;
import com.tianli.exception.Result;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.KLineConstants;
import com.tianli.kline.KLineService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.kline.task.HuoBiPeriodEnum;
import com.tianli.kline.task.KlineStatDTO;
import com.tianli.kline.vo.KlineTypeListVo;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.BianPriceCache;
import com.tianli.tool.MapTool;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * K线图数据 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-07
 */
@RestController
@RequestMapping("/kLine")
public class KLineController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private KLineService kLineService;

    @Resource
    private DigitalCurrencyExchangeComponent digitalCurrencyExchangeComponent;

    @Resource
    private ConfigService configService;

    @Resource
    private FollowCurrencyService followCurrencyService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/stat")
    public Result getKLine(@NotBlank(message = "交易对不能为空") String symbol,
                           @NotNull(message = "数据时间粒度不能为空") HuoBiPeriodEnum period,
                           Integer size) {
        size = Objects.isNull(size) ? KLineConstants.KLINE_DEFAULT_SIZE : size;
        BoundValueOperations<String, String> valueOperations = stringRedisTemplate.boundValueOps(KLineConstants.KLINE_CACHE_PREFIX + symbol + ":" + period + ":" + size);
        String cacheKlineData = valueOperations.get();
        if (StringUtils.isBlank(cacheKlineData)) {
            return Result.success(kLineService.getHuoBiKlineStat(symbol, period, size));
        }
        KlineStatDTO vos = new Gson().fromJson(cacheKlineData, KlineStatDTO.class);
        return Result.success(vos);
    }

    /**
     * 获取usdt的人民币价格
     */
    @GetMapping("/usdtcny/price")
    public Result usdtPrice() {
        return Result.success(digitalCurrencyExchangeComponent.usdtCnyPrice());
    }

    /**
     * 获取BF的人民币价格
     */
    @GetMapping("/BFcny/price")
    public Result BFPrice() {
        double price = digitalCurrencyExchangeComponent.usdtCnyPrice();
        String BF_usdt_rate = configService.getOrDefault(ConfigConstants.BF_USDT_RATE, "1");
        double BF_usdt = Double.parseDouble(BF_usdt_rate);
        return Result.success(MapTool.Map().put("usdt", BF_usdt).put("cny", price * BF_usdt));
    }

    /**
     * 关注币种的列表
     * Yapi: 获取币种价格
     */
    @GetMapping("/current/price")
    public Result price(String type) {
        List<FollowCurrency> followCurrencyType;
        if (StringUtils.isNotBlank(type)) {
            FollowCurrency one = followCurrencyService.getOne(new LambdaQueryWrapper<FollowCurrency>().eq(FollowCurrency::getSymbol, type));
            if (Objects.isNull(one)) return Result.success(Lists.newArrayList());
            followCurrencyType = Lists.newArrayList(one);
        } else {
            followCurrencyType = followCurrencyService.list(new LambdaQueryWrapper<FollowCurrency>().eq(FollowCurrency::getSelected, true).orderByAsc(FollowCurrency::getSort));
            if (CollectionUtils.isEmpty(followCurrencyType)) return Result.success(Lists.newArrayList());
        }
        List<CurrencyPriceVO> list = Lists.newArrayList();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        List<Object> priceList = operations.multiGet(followCurrencyType.stream().map(e -> e.getSymbol() + ":price").collect(Collectors.toList()));
        for (int i = 0; i < followCurrencyType.size(); i++) {
            Object cachePrice = priceList.get(i);
            FollowCurrency followCurrency = followCurrencyType.get(i);
            String symbol = followCurrency.getSymbol();
            CurrencyPriceVO priceVO = CurrencyPriceVO.builder()
                    .img(followCurrency.getImg())
                    .symbol(symbol)
                    .name(followCurrency.getName()).build();
            if (cachePrice != null) {
                priceVO.setPrice(Double.valueOf(cachePrice.toString()));
                list.add(priceVO);
                continue;
            }
            String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=" + symbol)).getStringResult();
            JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
            Double crawlPrice = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
            if (Objects.nonNull(crawlPrice)) {
                // 4 - 5分钟的过期时间, 打乱缓存过期时间
                operations.set(symbol + ":price", crawlPrice, ThreadLocalRandom.current().nextLong(240, 300), TimeUnit.SECONDS);
                priceVO.setPrice(crawlPrice);
            }
            list.add(priceVO);
        }
        return Result.success(list);
    }

    /**
     * 获取USDT的法币价格
     *
     * @param currency 法币名称
     * @see NationalCurrencyEnum
     */
    @GetMapping("/usdt/{currency}/price")
    public Result usdtPrice(@PathVariable("currency") NationalCurrencyEnum currency) {
        return Result.success(digitalCurrencyExchangeComponent.usdtPrice(currency));
    }

    @GetMapping("/klineTypeList")
    public Result klineTypeList(@RequestParam(defaultValue = "true") Boolean select, String name) {
        List<FollowCurrency> list = followCurrencyService.list(new LambdaQueryWrapper<FollowCurrency>()
                .eq(Objects.nonNull(select), FollowCurrency::getSelected, select)
                .like(StringUtils.isNotBlank(name), FollowCurrency::getSymbol, StringUtils.isNotBlank(name) ? name.replaceAll("/", "").toLowerCase() : "")
                .orderByAsc(FollowCurrency::getSort));
        List<KlineTypeListVo> result = list.stream()
                .map(followCurrency -> KlineTypeListVo.getKlineTypeListVo(followCurrency, BianPriceCache.getPrice(followCurrency.getSymbol().toUpperCase())))
                .collect(Collectors.toList());
        return Result.success(result);
    }

}

