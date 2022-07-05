package com.tianli.exchange.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import com.tianli.exchange.enums.KLinesRedisKey;
import com.tianli.exchange.fatcory.MarketHandlerFactory;
import com.tianli.exchange.push.DepthStream;
import com.tianli.exchange.service.IKLinesInfoService;
import com.tianli.exchange.util.CalendarUtil;
import com.tianli.exchange.vo.DepthVo;
import com.tianli.management.newcurrency.entity.NewCurrencyManagement;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/6/10 13:43
 */
@Component
public class DefaultCoinProcessor implements CoinProcessor {


    @Resource
    RedisTemplate redisTemplate;

    @Resource
    IKLinesInfoService ikLinesInfoService;

    @Resource
    RedisLock redisLock;

    @Resource
    MarketHandlerFactory marketHandlerFactory;

    @Resource
    INewCurrencyManagementService newCurrencyManagementService;

    @Resource
    Gson gson;


    @Override
    public KLinesInfo getKLinesInfo(String symbol) {
        Map<String, KLinesInfo> minuteKLines = getMinuteKLines(0);
        if (CollUtil.isNotEmpty(minuteKLines)) {
            return minuteKLines.get(symbol);
        }
        return null;
    }

    public Map<String, KLinesInfo> getKLinesInfo() {
        return getMinuteKLines(0);
    }

    private Map<String, KLinesInfo> getMinuteKLines(int amount) {
        String key = StrUtil.format(KLinesRedisKey.CURRENT_K_LINE_KEY, CalendarUtil.getM(amount));
        if (!redisTemplate.hasKey(key)) {
            createNewKLines(CalendarUtil.getM(-1));
        }
        Map map = redisTemplate.boundHashOps(key).entries();
        if (CollUtil.isNotEmpty(map)) {
            Map<String, KLinesInfo> result = new HashMap<>(map.size());
            for (Object o : map.keySet()) {
                result.put(o.toString(), JSONUtil.toBean(map.get(o).toString(), KLinesInfo.class));
            }
            return result;
        }
        return null;
    }

    private Map<String, KLinesInfo> getDayKLines(long time) {
        String key = StrUtil.format(KLinesRedisKey.CURRENT_DAY_K_LINE_KEY, time);
        if (!redisTemplate.hasKey(key)) {
            initDayKLines();
        }
        Map map = redisTemplate.boundHashOps(key).entries();
        if (CollUtil.isNotEmpty(map)) {
            Map<String, KLinesInfo> result = new HashMap<>(map.size());
            for (Object o : map.keySet()) {
                result.put(o.toString(), JSONUtil.toBean(map.get(o).toString(), KLinesInfo.class));
            }
            return result;
        }
        return new HashMap<>();
    }

    /**
     * 获取上一分钟的k线数据
     *
     * @return
     */
    public Map<String, KLinesInfo> getLastMinuteKLines() {
        return getMinuteKLines(-1);
    }


    @Override
    public KLinesInfo getDayKLinesInfoBySymbol(String symbol) {
        Map<String, KLinesInfo> dayKLinesInfo = getDayKLinesInfo(CalendarUtil.getCurrentTimeMs());
        if (CollUtil.isNotEmpty(dayKLinesInfo)) {
            return dayKLinesInfo.get(symbol);
        }
        return null;
    }

    @Override
    public Map<String, KLinesInfo> getDayKLinesInfo(Long time) {
        if (ObjectUtil.isNull(time) || time.equals(0L)) {
            time = CalendarUtil.getCurrentTimeMs();
        }
        return getDayKLines(time);
    }


    public void createNewKLines(long time) {
        String lock = StrUtil.format(KLinesRedisKey.CURRENT_K_LINE_LOCK, time);
        if (!redisLock._lock(lock, 1L, TimeUnit.MINUTES)) {
            //加锁失败延时2秒返回
            ThreadUtil.sleep(2000);
            return;
        }
        List<String> symbols = getOnlineTradingPair();
        if (CollUtil.isEmpty(symbols)) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        symbols.forEach(symbol -> map.put(symbol, JSONUtil.toJsonStr(KLinesInfo.getDefault(symbol, time, time + 59999, KLinesIntervalEnum.one))));
        String key = StrUtil.format(KLinesRedisKey.CURRENT_K_LINE_KEY, time);
        if (redisTemplate.hasKey(key)) {
            return;
        }
        BoundHashOperations hashOps = redisTemplate.boundHashOps(key);
        hashOps.putAll(map);
        hashOps.expire(5L, TimeUnit.MINUTES);
    }

    /**
     * 查询所有已经上架的平台币
     *
     * @return
     */
    @Override
    public List<String> getOnlineTradingPair() {
        List<NewCurrencyManagement> currencyManagements = newCurrencyManagementService.queryOnlineToken();
        if (CollUtil.isEmpty(currencyManagements)) {
            return null;
        }
        List<String> symbols = new ArrayList<>();
        currencyManagements.forEach(currencyManagement -> symbols.add(currencyManagement.getCurrency_name_short() + "USDT"));
        return symbols;
    }


    /**
     * 初始化日k线
     *
     * @param symbol
     * @return
     */
    public KLinesInfo initDayKLines(String symbol) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long firstTimeOfToday = calendar.getTimeInMillis();
        if (!redisLock._lock(StrUtil.format(KLinesRedisKey.CURRENT_SYMBOL_DAY_K_LINE_LOCK, symbol, firstTimeOfToday), 3L, TimeUnit.SECONDS)) {
            return null;
        }
        String redisKey = StrUtil.format(KLinesRedisKey.CURRENT_DAY_K_LINE_KEY, firstTimeOfToday);
        BoundHashOperations hashOps = redisTemplate.boundHashOps(redisKey);
        List<KLinesInfo> allKLine = ikLinesInfoService.findAllKLine(Arrays.asList(symbol), firstTimeOfToday, nowTime, KLinesIntervalEnum.one);
        if (CollUtil.isEmpty(allKLine)) {
            allKLine = new ArrayList<>();
        }
        Map<String, List<KLinesInfo>> kLineMap = allKLine.stream().collect(Collectors.groupingBy(KLinesInfo::getSymbol));
        KLinesInfo dayKLine = KLinesInfo.getDefault(symbol, firstTimeOfToday, firstTimeOfToday + 86399999, KLinesIntervalEnum.day);
        List<KLinesInfo> kLinesInfos = kLineMap.get(symbol);
        if (CollUtil.isNotEmpty(kLinesInfos)) {
            setKline(dayKLine, kLinesInfos);
        }
        if (ObjectUtil.isNull(dayKLine.getClosing_price()) || dayKLine.getClosing_price().compareTo(BigDecimal.ZERO) == 0) {
            KLinesInfo lastBySymbol = ikLinesInfoService.findLastBySymbol(symbol);
            if (ObjectUtil.isNotNull(lastBySymbol)) {
                dayKLine.setClosing_price(lastBySymbol.getClosing_price());
            } else {
                //没有K线表示第一次上线
                NewCurrencyManagement newCurrencyManagement = newCurrencyManagementService.getOne(Wrappers.lambdaQuery(NewCurrencyManagement.class)
                        .eq(NewCurrencyManagement::getCurrency_name_short, CurrencyCoinEnum.getTokenStock(symbol).getName()));
                if (ObjectUtil.isNotNull(newCurrencyManagement)) {
                    dayKLine.setClosing_price(newCurrencyManagement.getTrade_start_price());
                }
            }
        }
        hashOps.put(symbol, JSONUtil.toJsonStr(dayKLine));
        return dayKLine;
    }


    @Override
    public void initDayKLines() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long firstTimeOfToday = calendar.getTimeInMillis();
        if (!redisLock._lock(StrUtil.format(KLinesRedisKey.CURRENT_DAY_K_LINE_LOCK, firstTimeOfToday), 3L, TimeUnit.SECONDS)) {
            return;
        }
        String redisKey = StrUtil.format(KLinesRedisKey.CURRENT_DAY_K_LINE_KEY, firstTimeOfToday);
        if (redisTemplate.hasKey(redisKey)) {
            return;
        }
        BoundHashOperations hashOps = redisTemplate.boundHashOps(redisKey);
        List<String> symbols = getOnlineTradingPair();
        if (CollUtil.isEmpty(symbols)) {
            return;
        }
        List<KLinesInfo> allKLine = ikLinesInfoService.findAllKLine(symbols, firstTimeOfToday, nowTime, KLinesIntervalEnum.one);
        if (CollUtil.isEmpty(allKLine)) {
            allKLine = new ArrayList<>();
        }
        Map<String, List<KLinesInfo>> kLineMap = allKLine.stream().collect(Collectors.groupingBy(KLinesInfo::getSymbol));
        Map<String, String> kLine24hr = new HashMap<>();
        for (String symbol : symbols) {
            KLinesInfo dayKLine = KLinesInfo.getDefault(symbol, firstTimeOfToday, firstTimeOfToday + 86399999, KLinesIntervalEnum.day);
            List<KLinesInfo> kLinesInfos = kLineMap.get(symbol);
            if (CollUtil.isNotEmpty(kLinesInfos)) {
                setKline(dayKLine, kLinesInfos);
            }
            if (ObjectUtil.isNull(dayKLine.getClosing_price()) || dayKLine.getClosing_price().compareTo(BigDecimal.ZERO) == 0) {
                KLinesInfo lastBySymbol = ikLinesInfoService.findLastBySymbol(symbol);
                if (ObjectUtil.isNotNull(lastBySymbol)) {
                    dayKLine.setClosing_price(lastBySymbol.getClosing_price());
                } else {
                    //没有K线表示第一次上线
                    NewCurrencyManagement newCurrencyManagement = newCurrencyManagementService.getOne(Wrappers.lambdaQuery(NewCurrencyManagement.class)
                            .eq(NewCurrencyManagement::getCurrency_name_short, CurrencyCoinEnum.getTokenStock(symbol).getName()));
                    if (ObjectUtil.isNotNull(newCurrencyManagement)) {
                        dayKLine.setClosing_price(newCurrencyManagement.getTrade_start_price());
                    }
                }
            }
            kLine24hr.put(symbol, JSONUtil.toJsonStr(dayKLine));
        }
        hashOps.putAll(kLine24hr);
        hashOps.expire(25L, TimeUnit.HOURS);
    }

    @Override
    public KLinesInfo setKline(KLinesInfo kLine, List<KLinesInfo> kLinesInfos) {
        if (CollUtil.isEmpty(kLinesInfos)) {
            return kLine;
        }
        KLinesInfo old = KLinesInfo.getDefault(kLine.getSymbol(), kLine.getOpening_time(), kLine.getClosing_time(), KLinesIntervalEnum.one);
        for (KLinesInfo kLinesInfo : kLinesInfos) {
            boolean historyIsTrade = kLinesInfo.getVolume().compareTo(BigDecimal.ZERO) > 0;
            if (!historyIsTrade) {
                //直接排除掉没有交易的k线
                continue;
            }
            if (old.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
                old.setOpening_price(kLinesInfo.getOpening_price());
            }
            if (old.getOpening_trade_id().equals(0L) && ObjectUtil.isNotNull(kLinesInfo.getOpening_trade_id()) && kLinesInfo.getOpening_trade_id() > 0) {
                old.setOpening_trade_id(kLinesInfo.getOpening_trade_id());
            }
            if (kLinesInfo.getHighest_price().compareTo(old.getHighest_price()) > 0) {
                old.setHighest_price(kLinesInfo.getHighest_price());
            }
            if (old.getLowest_price().compareTo(BigDecimal.ZERO) == 0 || old.getLowest_price().compareTo(kLinesInfo.getLowest_price()) > 0) {
                old.setLowest_price(kLinesInfo.getLowest_price());
            }
            old.setClosing_price(kLinesInfo.getClosing_price());
            old.setClosing_trade_id(kLinesInfo.getClosing_trade_id());
            old.setVolume(old.getVolume().add(kLinesInfo.getVolume()));
            old.setTurnover(old.getTurnover().add(kLinesInfo.getTurnover()));
            old.setTurnover_num(old.getTurnover_num() + kLinesInfo.getTurnover_num());
            old.setActive_buy_volume(old.getActive_buy_volume().add(kLinesInfo.getActive_buy_volume()));
            old.setActive_buy_turnover(old.getActive_buy_turnover().add(kLinesInfo.getActive_buy_turnover()));
        }
        //没有交易的就设置上根k线的数据
        if (kLine.getVolume().compareTo(BigDecimal.ZERO) == 0 && old.getVolume().compareTo(BigDecimal.ZERO) == 0) {
            KLinesInfo kLinesInfo = kLinesInfos.get(0);
            setKline(kLine, kLinesInfo);
        } else if (kLine.getVolume().compareTo(BigDecimal.ZERO) == 0 && old.getVolume().compareTo(BigDecimal.ZERO) > 0) {
            setKline(kLine, old);
        } else if (kLine.getVolume().compareTo(BigDecimal.ZERO) > 0 && old.getVolume().compareTo(BigDecimal.ZERO) > 0) {
            kLine.setOpening_price(old.getOpening_price());
            kLine.setOpening_trade_id(old.getOpening_trade_id());
            kLine.setHighest_price(kLine.getHighest_price().compareTo(old.getHighest_price()) > 0 ? kLine.getHighest_price() : old.getHighest_price());
            kLine.setLowest_price(kLine.getLowest_price().compareTo(old.getLowest_price()) < 0 ? kLine.getLowest_price() : old.getLowest_price());
            kLine.setTurnover(kLine.getTurnover().add(old.getTurnover()));
            kLine.setVolume(kLine.getVolume().add(old.getVolume()));
            kLine.setTurnover_num(old.getTurnover_num());
            kLine.setActive_buy_volume(old.getActive_buy_volume());
            kLine.setActive_buy_turnover(old.getActive_buy_turnover());
        }
        return kLine;
    }

    private void setKline(KLinesInfo kLine, KLinesInfo kLinesInfo) {
        kLine.setOpening_price(kLinesInfo.getOpening_price());
        kLine.setOpening_trade_id(kLinesInfo.getOpening_trade_id());
        kLine.setHighest_price(kLinesInfo.getHighest_price());
        kLine.setLowest_price(kLinesInfo.getLowest_price());
        kLine.setClosing_price(kLinesInfo.getClosing_price());
        kLine.setClosing_trade_id(kLinesInfo.getClosing_trade_id());
        kLine.setTurnover(kLinesInfo.getTurnover());
        kLine.setVolume(kLinesInfo.getVolume());
        kLine.setTurnover_num(kLinesInfo.getTurnover_num());
        kLine.setActive_buy_volume(kLinesInfo.getActive_buy_volume());
        kLine.setActive_buy_turnover(kLinesInfo.getActive_buy_turnover());
    }


    public void autoGenerate() {
        //创建下一分钟的k线
        ThreadUtil.execute(() -> createNewKLines(CalendarUtil.getM(1)));
        Map<String, KLinesInfo> lastMinuteKLines = getLastMinuteKLines();
        if (CollUtil.isEmpty(lastMinuteKLines)) {
            return;
        }
        Map<String, KLinesInfo> dayKLines = getDayKLinesInfo(CalendarUtil.getCurrentTimeMs());
        List<KLinesInfo> addKLinesInfos = new ArrayList<>();
        for (String symbol : lastMinuteKLines.keySet()) {
            KLinesInfo kLinesInfo = lastMinuteKLines.get(symbol);
            if (kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
                KLinesInfo dayKLinesInfo = dayKLines.get(symbol);
                if (ObjectUtil.isNull(dayKLinesInfo)) {
                    //没有日k线则创建
                    dayKLinesInfo = initDayKLines(symbol);
                    if (ObjectUtil.isNull(dayKLinesInfo)) {
                        continue;
                    }
                }
                BigDecimal closingPrice = dayKLinesInfo.getClosing_price();
                kLinesInfo.setOpening_price(closingPrice);
                kLinesInfo.setHighest_price(closingPrice);
                kLinesInfo.setLowest_price(closingPrice);
                kLinesInfo.setClosing_price(closingPrice);

            }
            kLinesInfo.setCreate_time(LocalDateTimeUtil.now());
            addKLinesInfos.add(kLinesInfo);
        }
        ikLinesInfoService.addBatch(addKLinesInfos);
    }

    @Override
    public void generateKLine() {
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (minute % 5 == 0) {
            ThreadUtil.execute(() -> generateKLine(KLinesIntervalEnum.five, time, 5));
        }
        if (minute % 15 == 0) {
            ThreadUtil.execute(() -> generateKLine(KLinesIntervalEnum.fifteen, time, 15));
        }
        if (minute % 30 == 0) {
            ThreadUtil.execute(() -> generateKLine(KLinesIntervalEnum.thirty, time, 30));
        }
        if (minute % 60 == 0) {
            ThreadUtil.execute(() -> generateKLine(KLinesIntervalEnum.sixty, time, 60));
        }
        if (hour == 0 && minute == 0) {
            ThreadUtil.execute(this::addDayLinesInfo);
        }
    }

    @Override
    public void processTrade(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        setMKLine(coreResponseDTO);
        setDayKLine(coreResponseDTO);
    }

    @Override
    public void setDepth(DepthStream depthStream, String symbol) {
        BoundHashOperations depth = redisTemplate.boundHashOps("depth");
        depth.put(symbol, gson.toJson(depthStream));
    }

    @Override
    public DepthVo getDepth(String symbol) {
        DepthVo depthVo = DepthVo.builder().bids(new ArrayList<>()).asks(new ArrayList<>()).build();
        BoundHashOperations depth = redisTemplate.boundHashOps("depth");
        Object o = depth.get(symbol);
        if (ObjectUtil.isNotNull(o)) {
            DepthStream depthStream = gson.fromJson(o.toString(), DepthStream.class);
            depthVo.setAsks(depthStream.getA());
            depthVo.setBids(depthStream.getB());
        }
        return depthVo;
    }

    private void setMKLine(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        long m = CalendarUtil.getM(0);
        String key = StrUtil.format(KLinesRedisKey.CURRENT_K_LINE_KEY, m);
        if (!redisTemplate.hasKey(key)) {
            createNewKLines(m);
        }
        processTrade(coreResponseDTO, key);
    }

    private void setDayKLine(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        String key = StrUtil.format(KLinesRedisKey.CURRENT_DAY_K_LINE_KEY, CalendarUtil.getCurrentTimeMs());
        if (!redisTemplate.hasKey(key)) {
            initDayKLines(coreResponseDTO.getSymbol());
        }
        processTrade(coreResponseDTO, key);
    }

    private void processTrade(ExchangeCoreResponseConvertDTO coreResponseDTO, String key) {
        BoundHashOperations hashOps = redisTemplate.boundHashOps(key);
        Map map = hashOps.entries();
        if (CollUtil.isEmpty(map)) {
            return;
        }
        Object o = map.get(coreResponseDTO.getSymbol());
        if (ObjectUtil.isNull(o)) {
            return;
        }
        KLinesInfo kLinesInfo = JSONUtil.toBean(o.toString(), KLinesInfo.class);
        BigDecimal price = coreResponseDTO.getPrice();
        if (kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
            kLinesInfo.setOpening_price(price);
            kLinesInfo.setHighest_price(price);
            kLinesInfo.setLowest_price(price);
        } else {
            kLinesInfo.setHighest_price(kLinesInfo.getHighest_price().max(price));
            kLinesInfo.setLowest_price(kLinesInfo.getLowest_price().min(price));
        }
        kLinesInfo.setClosing_price(price);
        kLinesInfo.setVolume(kLinesInfo.getVolume().add(coreResponseDTO.getQuantity()));
        kLinesInfo.setTurnover(kLinesInfo.getTurnover().add(coreResponseDTO.getQuantity().multiply(coreResponseDTO.getPrice())));
        kLinesInfo.setTurnover_num(kLinesInfo.getTurnover_num() + 1);
        hashOps.put(coreResponseDTO.getSymbol(), JSONUtil.toJsonStr(kLinesInfo));
    }


    private void addDayLinesInfo() {
        //存储日线
        Calendar lastDay = Calendar.getInstance();
        lastDay.set(Calendar.SECOND, 0);
        lastDay.set(Calendar.MILLISECOND, 0);
        lastDay.set(Calendar.MINUTE, 0);
        lastDay.set(Calendar.HOUR_OF_DAY, 0);
        lastDay.add(Calendar.HOUR_OF_DAY, -24);
        long lastTime = lastDay.getTimeInMillis();
        Map<String, KLinesInfo> dayKLinesInfo = getDayKLinesInfo(lastTime);
        LocalDateTime now = LocalDateTime.now();
        List<KLinesInfo> addKLinesInfos = new ArrayList<>();
        if (CollUtil.isNotEmpty(dayKLinesInfo)) {
            for (String symbol : dayKLinesInfo.keySet()) {
                KLinesInfo kLinesInfo = dayKLinesInfo.get(symbol);
                BigDecimal closingPrice = kLinesInfo.getClosing_price();
                if (kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
                    kLinesInfo.setOpening_price(closingPrice);
                }
                if (kLinesInfo.getHighest_price().compareTo(BigDecimal.ZERO) == 0) {
                    kLinesInfo.setHighest_price(closingPrice);
                }
                if (kLinesInfo.getLowest_price().compareTo(BigDecimal.ZERO) == 0) {
                    kLinesInfo.setLowest_price(closingPrice);
                }
                kLinesInfo.setCreate_time(now);
                addKLinesInfos.add(kLinesInfo);
            }
            ikLinesInfoService.addBatch(addKLinesInfos);
        }
    }

    private void generateKLine(KLinesIntervalEnum kLinesIntervalEnum, long time, int range) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        long endTime = calendar.getTimeInMillis() - 60000;
        //往前推range - 1个时间单位
        calendar.add(Calendar.MINUTE, -range);
        long startTime = calendar.getTimeInMillis();
        List<String> symbols = getOnlineTradingPair();
        if (CollUtil.isEmpty(symbols)) {
            return;
        }
        List<KLinesInfo> kLine = ikLinesInfoService.findAllKLine(symbols, startTime, endTime, KLinesIntervalEnum.one);
        if (CollUtil.isEmpty(kLine)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<KLinesInfo> addKLinesInfos = new ArrayList<>();
        Map<String, List<KLinesInfo>> map = kLine.stream().collect(Collectors.groupingBy(KLinesInfo::getSymbol));
        for (String symbol : map.keySet()) {
            List<KLinesInfo> kLinesInfos = map.get(symbol);
            if (CollUtil.isEmpty(kLinesInfos)) {
                continue;
            }
            KLinesInfo kLinesInfo = KLinesInfo.getDefault(symbol, startTime, endTime + 59999, kLinesIntervalEnum);
            setKline(kLinesInfo, kLinesInfos);
            kLinesInfo.setCreate_time(now);
            addKLinesInfos.add(kLinesInfo);
        }
        ikLinesInfoService.addBatch(addKLinesInfos);
    }
}
