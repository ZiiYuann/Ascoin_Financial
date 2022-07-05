package com.tianli.bet.task;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotOrderService;
import com.tianli.robot.RobotResultService;
import com.tianli.robot.mapper.RobotResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class BetOrderTask {

    @Resource
    private BetService betService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    private static AtomicInteger  threadId = new AtomicInteger(1);

    private final static ExecutorService BET_TASK_EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1024),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("betTask-updateResult-" + threadId.getAndIncrement());
                return thread;
            },
            ScheduleTaskRejectedExecutionHandler.getInstance());

    private static AtomicInteger settleThreadId = new AtomicInteger(1);

    private final static ScheduledThreadPoolExecutor BET_SETTLE_EXECUTOR = new ScheduledThreadPoolExecutor(4,
            r -> {
                Thread thread = new Thread(r);
                thread.setName("settle-updateResult-" + settleThreadId.getAndIncrement());
                return thread;
            },
            ScheduleTaskRejectedExecutionHandler.getInstance());

    private static AtomicInteger pushDelayedThreadId = new AtomicInteger(1);
    private final static ScheduledThreadPoolExecutor PUSH_DELAYED_EXECUTOR = new ScheduledThreadPoolExecutor(1,
            r -> {
                Thread thread = new Thread(r);
                thread.setName("push-redis-delayed-" + pushDelayedThreadId.getAndIncrement());
                return thread;
            },
            ScheduleTaskRejectedExecutionHandler.getInstance());


    /**
     * 废弃
     */
    //    @Scheduled(cron = "0 0/1 * * * ?")
    public void periodicUpdateBetResult() {
        LocalDateTime now = LocalDateTime.now();
        String dayHourMin = String.format("%s_%s_%s_%s", now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute());
        String redisKey = "BetOrderTask#periodicUpdateBetResult-pageFlag-" + dayHourMin;
        BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisKey);
        operation.setIfAbsent(0, 5, TimeUnit.SECONDS);
        while (true) {
            Long page = operation.increment();
            if (page == null) {
                break;
            }
            Page<Bet> pageBet = betService.page(new Page<>(page, 20), new LambdaQueryWrapper<Bet>().isNotNull(Bet::getStart_exchange_rate).isNull(Bet::getComplete_time));
            List<Bet> records = pageBet.getRecords();
            int size;
            if ((size = records.size()) <= 0) {
                redisTemplate.delete(redisKey);
                break;
            }
            log.info(String.format("\n更新押注结果定时任务 ==> [%s],页码[%s], 任务数[%s], 详情ids[]", dayHourMin, page, size));
            // 查询当前k_line数据
            records.forEach(bet -> BET_TASK_EXECUTOR.execute(() -> betService.settleBet(bet.getId())));
        }
    }

    public void offerSchedule(Bet createBet) {
        Double bet_time = createBet.getBet_time();
        LocalDateTime create_time = createBet.getCreate_time();
        LocalDateTime complete_date_time = create_time.plusSeconds((long)(bet_time * 60));
        long betweenTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), complete_date_time);
        Long betId = createBet.getId();
        System.out.println(betweenTime + "S后, 订单:["+betId+"]执行押注结算");
        BET_SETTLE_EXECUTOR.schedule(() -> {
                    System.out.println("订单:["+betId+"]执行押注结算");
                    Bet bet = betService.getById(betId);
                    if(Objects.nonNull(bet) && Objects.equals(BetResultEnum.wait, bet.getResult())){
                        String lockKey = "BET_SETTLE_EXECUTOR:" + bet.getId();
                        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(lockKey);
                        Boolean lock = operations.setIfAbsent(UUID.randomUUID(), 3L, TimeUnit.MINUTES);
                        if(Objects.isNull(lock) || !lock){
                            System.out.println("订单:["+betId+"]执行押注结算-Return");
                            return;
                        }
                        betService.settleBet(bet);
                        redisTemplate.delete(lockKey);
                        System.out.println("订单:["+betId+"]执行押注结算-Success");
                    }
                },
                betweenTime,
                TimeUnit.SECONDS);
    }

    public void offerScheduleRedis(Long uid, Long betId) {
        PUSH_DELAYED_EXECUTOR.schedule(() -> {
                    String redisKey = "bet_result_uid_" + uid;
                    BoundListOperations<String, Object> listOps = redisTemplate.boundListOps(redisKey);
                    listOps.leftPush(betId);
                    listOps.expire(5, TimeUnit.MINUTES);
                },
                300,
                TimeUnit.MILLISECONDS);
    }

}
