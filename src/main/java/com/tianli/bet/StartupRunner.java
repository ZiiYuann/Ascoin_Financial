package com.tianli.bet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.task.BetOrderTask;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 优化项目启动, 当项目启动完成再加载动态开关信息
 * BaseSwitchController
 */
@Component
public class StartupRunner implements CommandLineRunner {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private BetOrderTask betOrderTask;

    @Resource
    private BetService betService;

    @Override
    public void run(String... args) {
        try {
            String redisLockKey = "UN_SETTLE_BET_SELECT_KEY";
            BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisLockKey);
            operation.setIfAbsent(0, 5, TimeUnit.SECONDS);
            while (true) {
                Long page = operation.increment();
                if (page == null) {
                    break;
                }
                Page<Bet> pageBet = betService.page(new Page<>(page, 20), new LambdaQueryWrapper<Bet>()
                        .isNotNull(Bet::getStart_exchange_rate)
                        .isNull(Bet::getComplete_time));
                List<Bet> records = pageBet.getRecords();
                if ((records.size()) <= 0) {
                    redisTemplate.delete(redisLockKey);
                    break;
                }
                records.forEach(e -> betOrderTask.offerSchedule(e));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}