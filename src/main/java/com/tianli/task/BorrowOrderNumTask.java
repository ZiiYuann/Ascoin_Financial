package com.tianli.task;

import com.tianli.borrow.service.IBorrowOrderNumDailyService;
import com.tianli.common.RedisLockConstants;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Log4j2
public class BorrowOrderNumTask {
    @Autowired
    private IBorrowOrderNumDailyService borrowOrderNumDailyService;

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticalOrderNum(){
        RLock lock = redissonClient.getLock(RedisLockConstants.BORROW_ORDER_NUM_TASK);
        if(lock.tryLock()){
            borrowOrderNumDailyService.statisticalOrderNum();
        }
    }
}
