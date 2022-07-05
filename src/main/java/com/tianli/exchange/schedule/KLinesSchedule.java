package com.tianli.exchange.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import com.tianli.exchange.enums.KLinesRedisKey;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.push.QuotesPush;
import com.tianli.exchange.service.IKLinesInfoService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/6/9 15:48
 */
@Component
public class KLinesSchedule {

    @Resource
    IKLinesInfoService kLinesInfoService;

    @Resource
    AsyncService asyncService;

    @Resource
    RedisLock redisLock;

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    QuotesPush quotesPush;


    /**
     * 每分钟定时器，处理分钟K线
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void autoGenerate() {
        asyncService.async(() -> {
            if (!redisLock._lock(KLinesRedisKey.AUTO_GENERATE_LOCK, 1L, TimeUnit.MINUTES)) {
                return;
            }
            try {
                //生成1分钟k线
                coinProcessor.autoGenerate();
                coinProcessor.generateKLine();
            } finally {
                redisLock.unlock();
            }
        });
    }


    @Scheduled(fixedDelay = 2000)
    public void pushKline() {
        asyncService.async(() -> {
            if (!redisLock._lock(KLinesRedisKey.PUSH_K_LINES_LOCK, 1L, TimeUnit.MINUTES)) {
                return;
            }
            Map<String, KLinesInfo> dayKLinesInfos = coinProcessor.getDayKLinesInfo(KLinesIntervalEnum.getIntervalTime(KLinesIntervalEnum.day));
            Map<String, KLinesInfo> map = coinProcessor.getKLinesInfo();
            if (CollUtil.isEmpty(map)) {
                return;
            }
            for (String symbol : map.keySet()) {
                KLinesInfo kLinesInfo = map.get(symbol);
                KLinesInfo dayKLinesInfo = dayKLinesInfos.get(symbol);
                if (ObjectUtil.isNull(dayKLinesInfo)) {
                    continue;
                }
                BigDecimal closing_price = dayKLinesInfo.getClosing_price();
                if (kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) == 0) {
                    kLinesInfo.setOpening_price(closing_price);
                    kLinesInfo.setHighest_price(closing_price);
                    kLinesInfo.setLowest_price(closing_price);
                    kLinesInfo.setClosing_price(closing_price);
                }
            }
            List<String> symbols = coinProcessor.getOnlineTradingPair();
            for (KLinesIntervalEnum kLinesIntervalEnum : KLinesIntervalEnum.values()) {
                long intervalTime = KLinesIntervalEnum.getIntervalTime(kLinesIntervalEnum);
                switch (kLinesIntervalEnum) {
                    case one:
                        ThreadUtil.execute(() -> quotesPush.pushKLinesInfos(map, kLinesIntervalEnum));
                        break;
                    case five:
                    case fifteen:
                    case thirty:
                    case sixty:
                        ThreadUtil.execute(() -> pushKLinesInfos(symbols, kLinesIntervalEnum, intervalTime, map));
                        break;
                    case day:
                        Map<String, KLinesInfo> dayKLinesInfo = coinProcessor.getDayKLinesInfo(intervalTime);
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
                        }
                        ThreadUtil.execute(() -> quotesPush.pushKLinesInfos(dayKLinesInfo, kLinesIntervalEnum));
                        break;
                }
            }
        });
    }

    private void pushKLinesInfos(List<String> symbols, KLinesIntervalEnum kLinesIntervalEnum, long intervalTime, Map<String, KLinesInfo> map) {
        List<KLinesInfo> kLine = kLinesInfoService.findAllKLine(symbols, intervalTime, null, KLinesIntervalEnum.one);
        Map<String, List<KLinesInfo>> collect = new HashMap<>();
        if (CollUtil.isNotEmpty(kLine)) {
            collect = kLine.stream().collect(Collectors.groupingBy(KLinesInfo::getSymbol));
        }
        Map<String, KLinesInfo> pushKLinesInfos = new HashMap<>();
        for (String symbol : map.keySet()) {
            KLinesInfo pushKLinesInfo = BeanUtil.copyProperties(map.get(symbol), KLinesInfo.class);
            pushKLinesInfo.setOpening_time(intervalTime);
            pushKLinesInfo.setClosing_time(intervalTime + (kLinesIntervalEnum.getMinute() * 60000) - 1);
            pushKLinesInfo.setInterval(kLinesIntervalEnum.getInterval());
            List<KLinesInfo> kLinesInfos = collect.get(symbol);
            if (CollUtil.isNotEmpty(kLinesInfos)) {
                pushKLinesInfo = coinProcessor.setKline(pushKLinesInfo, kLinesInfos);
            }
            pushKLinesInfos.put(symbol, pushKLinesInfo);
        }
        quotesPush.pushKLinesInfos(pushKLinesInfos, kLinesIntervalEnum);
    }


}
