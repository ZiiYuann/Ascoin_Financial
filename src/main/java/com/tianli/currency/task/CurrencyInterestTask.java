package com.tianli.currency.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.Constants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.entity.Currency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CurrencyInterestTask {
    public static final String BSC_BLOCK_COUNT = "bsc_block_count";

    public static final String ETH_BLOCK_COUNT = "eth_block_count";

    @Resource
    private CurrencyService currencyService;

    @Resource
    private Gson gson;

    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GraphService graphService;

    @Resource
    private AddressService addressService;

    @Resource
    private RedisLock redisLock;

    @Resource
    private CurrencyInterestTask currencyInterestTask;

    @Resource
    private AsyncService asyncService;

    private static AtomicInteger threadId = new AtomicInteger(1);

    private static ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

    private final static ScheduledThreadPoolExecutor CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("CurrencyInterestTask#currencyInterestStat-" + threadId.getAndIncrement());
                return thread;
            }
    );


    /**
     * 每日计算利息task
     */
//    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void currencyInterestStat() {
        asyncService.async(() -> {
            LocalDateTime now = LocalDateTime.now();
            String day = String.format("%s_%s", now.getMonthValue(), now.getDayOfMonth());
            String redisKey = "CurrencyInterestTask#currencyInterestStat:pageFlag:" + day;
            BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisKey);
            operation.setIfAbsent(0, 1, TimeUnit.HOURS);
            while (true) {
                Long page = operation.increment();
                if (page == null) {
                    break;
                }
                Page<Currency> currencyPage = currencyService.page(new Page<>(page, 20), new LambdaQueryWrapper<Currency>()
                        .eq(Currency::getType, CurrencyTypeEnum.normal)
                );
                List<Currency> records = currencyPage.getRecords();
                if ((records.size()) <= 0) {
                    break;
                }
                String user_balance_daily_rate = configService._get(ConfigConstants.USER_BALANCE_DAILY_RATE);
                if (StringUtils.isBlank(user_balance_daily_rate)) {
                    break;
                }
                double rate;
                try {
                    rate = Double.valueOf(user_balance_daily_rate);
                } catch (NumberFormatException e) {
                    log.warn("\n用户余额日利率解析失败", e);
                    return;
                }
                if(rate <= 0){
                    return;
                }
                for (Currency c : records) {
                    interestStat(c, rate);
                }
            }
        });
    }

    /**
     * 统计每日利息
     */
    private void interestStat(Currency currency, double rate) {
        try {
            // 1. 计算利息
            BigDecimal bigDecimal = new BigDecimal(currency.getRemain());
            // 真是的今日利息数额
            BigDecimal dayInterest = bigDecimal.multiply(new BigDecimal(String.valueOf(rate)));
            BigInteger dayInterestBigInteger = dayInterest.toBigInteger();
            if(dayInterestBigInteger.compareTo(BigInteger.ZERO) <= 0){
                return;
            }
            // 2. 更新余额
            currencyService.increase(currency.getUid(), currency.getType(), dayInterestBigInteger, TimeTool.getDateTimeDisplayString(LocalDateTime.now()), CurrencyLogDes.利息.name());
        } catch (Exception e) {
            String toJson = gson.toJson(currency);
            log.warn("统计每日利息异常: currency:{}, rate:{}", toJson, rate, e);
            CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = FAIL_COUNT_CACHE.get(String.valueOf(currency.getId()));
                if (Objects.isNull(atomicInteger)) {
                    atomicInteger = new AtomicInteger(3);
                    FAIL_COUNT_CACHE.put(String.valueOf(currency.getId()), atomicInteger);
                }
                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    interestStat(currency, rate);
                } else {
                    log.error("统计每日利息失败: currency:{}, rate:{}", toJson, rate);
                }
            }, 30, TimeUnit.MINUTES);
        }
    }

}
