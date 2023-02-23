package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.enums.PledgeStatus;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.service.BorrowRecordService;
import com.tianli.product.aborrow.service.BorrowService;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
public class BorrowTask {

    @Resource
    private BorrowRecordService borrowRecordService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private BorrowService borrowService;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private RedissonClientTool redissonClientTool;

    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void task() {
        String key = RedisConstants.BORROW_TASK + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        atomicLong.expireIfNotSet(Duration.ofMinutes(1));
        long pagePre = atomicLong.get();
        while (true) {
            try {
                long page = atomicLong.getAndDecrement();
                var borrowRecords = borrowRecordService.page(new Page<>(page, 10)
                        , new LambdaQueryWrapper<BorrowRecord>()
                                .in(BorrowRecord::getPledgeStatus, List.of(PledgeStatus.PROCESS))).getRecords();
                if (CollectionUtils.isEmpty(borrowRecords)) {
                    break;
                }
                borrowRecords.forEach(borrowRecord -> {
                    final BorrowRecord newRecord = borrowService.calPledgeRate(borrowRecord);

                    BigDecimal currencyPledgeRate = newRecord.getCurrencyPledgeRate();
                    BigDecimal warnPledgeRate = newRecord.getWarnPledgeRate();
                    BigDecimal lqPledgeRate = newRecord.getLqPledgeRate();
                    if (currencyPledgeRate.compareTo(warnPledgeRate) < 0) {
                        return;
                    }
                    // todo 通知用户
                    if (!borrowRecord.isAutoReplenishment()) {
                        return;
                    }
                    String lockKey = RedisLockConstants.LOCK_BORROW + newRecord.getUid();
                    if (currencyPledgeRate.compareTo(lqPledgeRate) < 0) {
                        redissonClientTool.tryLock(lockKey, () -> borrowService.autoReplenishment(newRecord)
                                , ErrorCodeEnum.SYSTEM_BUSY, 30, TimeUnit.SECONDS, true);
                        return;
                    }
                    redissonClientTool.tryLock(lockKey, () -> borrowService.forcedCloseout(newRecord)
                            , ErrorCodeEnum.SYSTEM_BUSY, 30, TimeUnit.SECONDS, true);

                });

            } catch (Exception e) {
                // 防止异常导致部分记录未计算
                WebHookService.send("借贷定时任务异常：" + key + "  page：" + pagePre);
            }
        }
    }

    @Scheduled(cron = "0 0 1/1 * * ?")
    public void interestTask() {
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.BORROW_TASK_INTEREST + now.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        atomicLong.expireIfNotSet(Duration.ofMinutes(1));
        long pagePre = atomicLong.get();
        while (true) {
            try {
                long page = atomicLong.getAndDecrement();
                var borrowRecords = borrowRecordService.page(new Page<>(page, 10)
                        , new LambdaQueryWrapper<BorrowRecord>()
                                .select(BorrowRecord::getId, BorrowRecord::getUid)
                                .in(BorrowRecord::getPledgeStatus, List.of(PledgeStatus.PROCESS))).getRecords();
                if (CollectionUtils.isEmpty(borrowRecords)) {
                    break;
                }
                borrowRecords.forEach(borrowRecord ->
                        borrowRecordCoinService.calInterest(borrowRecord.getUid(), borrowRecord.getId()));
            } catch (Exception e) {
                // 防止异常导致部分记录未计算
                WebHookService.send("借贷定时利息任务异常：" + key + "  page：" + pagePre);
            }
        }
    }

}
