package com.tianli.loan.task;

import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.loan.service.ILoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @date 2022/5/26 22:49
 */
@Component
@Slf4j
public class LoanSchedule {

    private final String LOAN_TIME_KEY = "loan_time_key";

    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;

    @Resource
    ILoanService loanService;


    @Scheduled(cron = "0 */1 * * * ?")
    public void loanTime() {
        asyncService.async(() -> {
            if (!redisLock._lock(LOAN_TIME_KEY, 60L, TimeUnit.SECONDS)) {
                return;
            }
            try {
                loanService.loanTime();
            } finally {
                redisLock.unlock();
            }
        });
    }
}
