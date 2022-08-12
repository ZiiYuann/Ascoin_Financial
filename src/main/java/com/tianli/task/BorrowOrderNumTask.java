package com.tianli.task;

import com.tianli.borrow.service.IBorrowOrderNumDailyService;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@Log4j2
public class BorrowOrderNumTask {
    @Autowired
    private IBorrowOrderNumDailyService borrowOrderNumDailyService;

    @Autowired
    private RedisLock redisLock;

    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticalOrderNum(){
        redisLock.lock(RedisLockConstants.BORROW_ORDER_NUM_TASK,10L, TimeUnit.SECONDS);
        borrowOrderNumDailyService.statisticalOrderNum();
    }
}
