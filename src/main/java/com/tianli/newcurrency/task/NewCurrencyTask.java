package com.tianli.newcurrency.task;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.newcurrency.INewCurrencyDayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NewCurrencyTask {
    @Autowired
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Autowired
    private INewCurrencyDayService iNewCurrencyDayService;
    private static final String SYNC_CURRENCY_DAY_INSERT_TASK_KEY = "NewCurrencyDayController:syncSaveNewCurrency";
    private static final String SYNC_CURRENCY_DAY_COMPUTED_TASK_KEY = "NewCurrencyDayController:syncComputedNewCurrency";

    //每日0点保存一次用户余额
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncSaveNewCurrency() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(SYNC_CURRENCY_DAY_INSERT_TASK_KEY, 15L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                iNewCurrencyDayService.syncSaveCurrency();
            } catch (Exception e) {
                log.error("syncSaveNewCurrency() Exception:", e);
            } finally {
                redisLock.unlock(SYNC_CURRENCY_DAY_INSERT_TASK_KEY);
            }
        });
    }
    //分发计算
    @Scheduled(cron = "0 */5 * * * ?") //每5分钟执行一次
    public void syncComputedNewCurrency() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(SYNC_CURRENCY_DAY_COMPUTED_TASK_KEY, 15L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                iNewCurrencyDayService.syncComputedCurrency();
            } catch (Exception e) {
                log.error("syncComputedNewCurrency() Exception:", e);
            } finally {
                redisLock.unlock(SYNC_CURRENCY_DAY_COMPUTED_TASK_KEY);
            }
        });
    }
}
