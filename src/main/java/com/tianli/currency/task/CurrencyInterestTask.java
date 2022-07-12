package com.tianli.currency.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.ChargeService;
import com.tianli.common.ConfigConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class CurrencyInterestTask {

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private AsyncService asyncService;

    private final static AtomicInteger threadId = new AtomicInteger(1);

    private final static ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

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
                Page<AccountBalance> currencyPage = accountBalanceService.page(new Page<>(page, 20));
                List<AccountBalance> records = currencyPage.getRecords();
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
                for (AccountBalance c : records) {
                    interestStat(c, rate);
                }
            }
        });
    }

    /**
     * 统计每日利息
     */
    private void interestStat(AccountBalance accountBalanceBalance, double rate) {
        try {
            // 1. 计算利息
            // 真是的今日利息数额
            BigDecimal dayInterest = accountBalanceBalance.getRemain().multiply(new BigDecimal(String.valueOf(rate)));
            if(dayInterest.compareTo(BigDecimal.ZERO) <= 0){
                return;
            }
            // 2. 更新余额
            accountBalanceService.increase(accountBalanceBalance.getUid(), AccountChangeType.normal,dayInterest,
                    TimeTool.getDateTimeDisplayString(LocalDateTime.now()), CurrencyLogDes.利息.name());
        } catch (Exception e) {
            String toJson = gson.toJson(accountBalanceBalance);
            log.warn("统计每日利息异常: currency:{}, rate:{}", toJson, rate, e);
            CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = FAIL_COUNT_CACHE.get(String.valueOf(accountBalanceBalance.getId()));
                if (Objects.isNull(atomicInteger)) {
                    atomicInteger = new AtomicInteger(3);
                    FAIL_COUNT_CACHE.put(String.valueOf(accountBalanceBalance.getId()), atomicInteger);
                }
                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    interestStat(accountBalanceBalance, rate);
                } else {
                    log.error("统计每日利息失败: currency:{}, rate:{}", toJson, rate);
                }
            }, 30, TimeUnit.MINUTES);
        }
    }

}
