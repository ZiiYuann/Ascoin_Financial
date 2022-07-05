package com.tianli.schedule;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.kline.KLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @date 2022/4/29 11:54
 */
@Component
@Slf4j
public class BianTask {

    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;

    @Resource
    KLineService kLineService;

    @Scheduled(fixedDelay = 1000 * 30)
    public void bian24HrPriceSync() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("bianTask:bian24HrPriceSync", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                kLineService.getCurrentBianPriceList24hr();
            } catch (Exception e) {
                log.error("bianTask:bian24HrPriceSync Exception:", e);
            } finally {
                redisLock.unlock("bianTask:bian24HrPriceSync");
            }
        });
    }
}
