package com.tianli.kline;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.common.HttpUtils;
import com.tianli.currency.DigitalCurrencyExchangeComponent;
import com.tianli.kline.mapper.KLine;
import com.tianli.kline.mapper.KLineMapper;
import com.tianli.kline.mapper.TradingPairEnum;
import com.tianli.kline.task.HuoBiPeriodEnum;
import com.tianli.kline.task.KlineMacd;
import com.tianli.kline.task.KlineStatDTO;
import com.tianli.kline.task.Stat;
import com.tianli.tool.Bian24HrInfo;
import com.tianli.tool.BianPriceCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * K线图数据 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-07
 */
@Service
public class KLineService extends ServiceImpl<KLineMapper, KLine> {

    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    @Resource
    private Gson gson;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DigitalCurrencyExchangeComponent digitalCurrencyExchangeComponent;

    public KLine getByTime(LocalDateTime time) {
        return getByTime(time, TradingPairEnum.eth_usdt);
    }

    public KLine getByMinTime(LocalDateTime time) {
        return getByMinTime(time, TradingPairEnum.eth_usdt);
    }

    public KLine getByMinTime(LocalDateTime time, TradingPairEnum pair) {
        return super.getOne(new LambdaQueryWrapper<KLine>()
                .orderByAsc(KLine::getId)
                .eq(KLine::getPair, pair)
                .ge(KLine::getCreate_time, time)
                .last("LIMIT  1"));
    }

    public KLine getByTime(LocalDateTime time, TradingPairEnum pair) {
        return super.getOne(new LambdaQueryWrapper<KLine>()
                .orderByDesc(KLine::getId)
                .eq(KLine::getPair, pair)
                .le(KLine::getCreate_time, time)
                .last("LIMIT  1"));
    }

    public KlineStatDTO getHuoBiKlineStat(String symbol, HuoBiPeriodEnum period, Integer size) {
        Map<String, String> params = Maps.newHashMap();
        params.put("symbol", symbol);
        params.put("period", period.getDes());
        if (Objects.nonNull(size)) {
            params.put("size", String.valueOf(size));
        }
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.KLINE_HOST, KLineConstants.KLINE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n KLineService#getHuoBiKlineStat exe error ! !");
        }
        KlineStatDTO klineStatVO = gson.fromJson(dataJson, KlineStatDTO.class);
        if (Objects.nonNull(klineStatVO) && Objects.equals(klineStatVO.getStatus(), "ok")) {
            List<Stat> data = klineStatVO.getData();
            if (!CollectionUtils.isEmpty(data)) {
                String hashKey = String.format("%s_%s", KLineConstants.STAT_DATA_CACHE_KEY, period.getDes());
                BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(hashKey);
                Map<Object, Object> entries = hashOperations.entries();
                Stat stat = data.get(0);
                double sum10 = 0;
                double sum30 = 0;
                double sum60 = 0;
                double ema12 = -1;
                double ema26 = -1;
                double dea = -1;
                int maxLength = data.size();
                for(int i = maxLength - 1; i >= 0; i --){
                    // 1. 计算设置MA10, MA30, MA60
                    Stat start = data.get(i);
                    sum10 = sum10 + start.getClose();
                    sum30 = sum30 + start.getClose();
                    sum60 = sum60 + start.getClose();
                    if(i + 10 < maxLength){
                        sum10 = sum10 - data.get(i + 10).getClose();
                    }
                    if(i + 30 < maxLength){
                        sum30 = sum30 - data.get(i + 30).getClose();
                    }
                    if(i + 60 < maxLength){
                        sum60 = sum60 - data.get(i + 60).getClose();
                    }
                    start.setMa10(sum10 / (maxLength - i > 10 ? 10 : maxLength - i));
                    start.setMa30(sum30 / (maxLength - i > 30 ? 30 : maxLength - i));
                    start.setMa60(sum60 / (maxLength - i > 60 ? 60 : maxLength - i));
                    // 2. 押注统计
                    Object statData = entries.get(Integer.valueOf(start.getId().toString()));
                    if (Objects.nonNull(statData)) {
                        start.setBetStat(Double.valueOf(statData.toString()));
                    }
                    // 3. macd计算 EMA12, EMA26, DIFF, DEA(macd), BAR
                    KlineMacd macdCache = null;
                    if (ema12 < 0 && ema26 < 0 && dea < 0) {
                        macdCache = period.getMacdCache(stringRedisTemplate);
                        ema12 = macdCache.getEma12().get(period.getDes());
                        ema26 = macdCache.getEma26().get(period.getDes());
                        dea = macdCache.getDea().get(period.getDes());
                    }
                    ema12 = 2 * start.getClose() / 13 + 11 * ema12 / 13;
                    ema26 = 2 * start.getClose() / 27 + 25 * ema26 / 27;
                    double diff = ema12 - ema26;
                    dea = 2 * diff / 10 + 8 * dea / 10;
                    start.setEma12(ema12);
                    start.setEma26(ema26);
                    start.setDiff(diff);
                    start.setDea(dea);
                    start.setDea(dea);
                    start.setBar((diff - dea) * 2);
                    if (Objects.nonNull(macdCache)) {
                        macdCache.getEma12().put(period.getDes(), ema12);
                        macdCache.getEma26().put(period.getDes(), ema26);
                        macdCache.getDea().put(period.getDes(), dea);
                        period.flushMacdCache(stringRedisTemplate, macdCache);
                    }
                }
                Double close = stat.getClose();
                klineStatVO.setCurrentEth(close);
                double v = digitalCurrencyExchangeComponent.ethCnyPrice();
                klineStatVO.setCurrentCny(v);
                String date = map.get("date");
                LocalDateTime localDateTime = LocalDate.now().atTime(0, 0, 0);
                long second = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
                Double openDay = null;
                if (StringUtils.isBlank(date) || !StringUtils.equals(date, "" + second)) {
                    Stat todayCache = data.stream().filter(e -> e.getId() == second).findFirst().orElse(null);
                    if(Objects.isNull(todayCache)){
                        Stat todayCache_ = getOldestStatOfTheDay(params, second);
                        if (Objects.nonNull(todayCache_)) openDay = todayCache_.getOpen();
                    } else {
                        openDay = todayCache.getOpen();
                    }
                    if (Objects.nonNull(openDay)) {
                        map.put("date", "" + second);
                        map.put("vol", "" + openDay);
                    }
                } else {
                    openDay = Double.valueOf(map.get("vol"));
                }
                if(Objects.isNull(openDay)) openDay = 1000.0;
                klineStatVO.setRate((close - openDay) / openDay);
            }
            // 保存redis
            BoundValueOperations<String, String> boundValueOps = stringRedisTemplate.boundValueOps(KLineConstants.KLINE_CACHE_PREFIX + symbol + ":" + period + ":" + KLineConstants.KLINE_DEFAULT_SIZE);
            boundValueOps.set(gson.toJson(klineStatVO), period.getDurationTime(), TimeUnit.SECONDS);
            if(period == HuoBiPeriodEnum.onemin){
                // 保存简化的统计数据
                KlineStatDTO statDTO = KlineStatDTO.builder()
                        .currentCny(klineStatVO.getCurrentCny())
                        .currentEth(klineStatVO.getCurrentEth())
                        .rate(klineStatVO.getRate())
                        .build();
                BoundValueOperations<String, String> boundValueOps_ = stringRedisTemplate.boundValueOps(KLineConstants.KLINE_CACHE_PREFIX + "ethusdt:onemin:simple:stat");
                boundValueOps_.set(gson.toJson(statDTO), 10, TimeUnit.SECONDS);
            }
        }
        return klineStatVO;
    }

    /**
     * 获取当日最早的kline开始数据
     */
    private Stat getOldestStatOfTheDay(Map<String, String> params, long second) {
        String dataJsonTemp = null;
        try {
            params.put("size", "1");
            params.put("period", "1day");
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.KLINE_HOST, KLineConstants.KLINE_PATH, "GET", Maps.newHashMap(), params);
            dataJsonTemp = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 获取当日最早的kline开始数据 exe error ! !");
        }
        KlineStatDTO klineStatVOTemp = gson.fromJson(dataJsonTemp, KlineStatDTO.class);
        return klineStatVOTemp.getData().stream().filter(e -> e.getId() == second).findFirst().orElse(null);
    }


    /**
     * 抓取火币价格
     * @param klineType 币种
     * @return
     */
    public JsonObject crawlCurrentPrice(String klineType) {
        Map<String, String> params = Maps.newHashMap();
        params.put("symbol", klineType.replaceAll("/", "").toLowerCase());
        params.put("period", "1min");
        params.put("size", "1");
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.KLINE_HOST, KLineConstants.KLINE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 押注获取押注的当前汇率异常 ! !");
        }
        return new Gson().fromJson(dataJson, JsonObject.class);
    }

    /**
     * 抓取火币价格
     * @param klineType 币种
     * @return
     */
    public Stat crawlCurrentStatData(String klineType) {
        Map<String, String> params = Maps.newHashMap();
        params.put("symbol", klineType.replaceAll("/", "").toLowerCase());
        params.put("period", "1min");
        params.put("size", "1");
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.KLINE_HOST, KLineConstants.KLINE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 押注获取押注的当前汇率异常 ! !");
        }
        KlineStatDTO jsonObject = new Gson().fromJson(dataJson, KlineStatDTO.class);
        List<Stat> data = jsonObject.getData();
        return CollectionUtils.isEmpty(data) ? null : data.get(0);
    }

    /**
     * 抓取币安价格
     * @param klineType 币种
     * @return
     */
    public Stat crawlCurrentBianPrice(String klineType) {
        Map<String, String> params = Maps.newHashMap();
        params.put("symbol", klineType.replaceAll("/", "").toUpperCase());
        params.put("interval", "1m");
        params.put("limit", "1");
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.BIAN_KLINE_HOST, KLineConstants.BIAN_KLINE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 押注获取押注的当前汇率异常 ! !");
        }
        JsonArray json = new Gson().fromJson(dataJson, JsonArray.class);
        JsonArray elements = json.get(0).getAsJsonArray();
        Double open = elements.get(1).getAsDouble();
        Double high = elements.get(2).getAsDouble();
        Double low = elements.get(3).getAsDouble();
        Double close = elements.get(4).getAsDouble();
        Stat stat = new Stat();
        stat.setOpen(open);
        stat.setHigh(high);
        stat.setLow(low);
        stat.setClose(close);
        return stat;
    }

    /**
     * 抓取币安价格
     * @param klineType 币种
     * @return
     */
    public BigDecimal getCurrentBianPrice(String klineType) {
        Map<String, String> params = Maps.newHashMap();
        params.put("symbol", klineType.toUpperCase());
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.BIAN_KLINE_HOST, KLineConstants.BIAN_KLINE_PRICE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 获取当前价格异常 ! !");
        }
        JsonObject json = new Gson().fromJson(dataJson, JsonObject.class);
        JsonObject elements = json.getAsJsonObject();
        if (Objects.isNull(elements)) {
            return BigDecimal.ZERO;
        }
        JsonElement jsonElement = elements.get("price");
        if (ObjectUtil.isNull(jsonElement)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(jsonElement.getAsDouble());
    }

    /**
     * 抓取币安所有币价格
     * @return
     */
    public JsonArray getCurrentBianPriceList() {
        Map<String, String> params = Maps.newHashMap();
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.BIAN_KLINE_HOST, KLineConstants.BIAN_KLINE_PRICE_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 获取当前价格异常 ! !");
        }
        return new Gson().fromJson(dataJson, JsonArray.class);
    }

    /**
     * 抓取币安24小时价格变动数据
     * @return
     */
    public JsonArray getCurrentBianPriceList24hr() {
        Map<String, String> params = Maps.newHashMap();
        String dataJson = null;
        try {
            HttpResponse httpResponse = HttpUtils.doGet(KLineConstants.BIAN_KLINE_HOST, KLineConstants.BIAN_KLINE_24HR_PATH, "GET", Maps.newHashMap(), params);
            dataJson = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            log.warn("\n 获取当前价格异常 ! !");
            return null;
        }
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(dataJson, JsonArray.class);
        for (JsonElement jsonElement : jsonArray) {
            Bian24HrInfo bian24HrInfo = gson.fromJson(jsonElement, Bian24HrInfo.class);
            BianPriceCache.setPrice(bian24HrInfo.getSymbol(),bian24HrInfo);
        }
        return jsonArray;
    }
}
