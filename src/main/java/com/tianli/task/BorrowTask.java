package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedissonClientTool;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.enums.HedgeStatus;
import com.tianli.product.aborrow.enums.PledgeStatus;
import com.tianli.product.aborrow.service.BorrowHedgeEntrustService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.service.BorrowRecordService;
import com.tianli.product.aborrow.service.BorrowService;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
@Component
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
    @Resource
    private WebHookService webHookService;
    @Resource
    private BorrowHedgeEntrustService borrowHedgeEntrustService;

    //    @Scheduled(cron = "0 0 0 1/1 * ? ")  // 1min
//    @Scheduled(cron = "0/1 * * * * ? ") // 1s
    public void task() {
        String key = RedisConstants.BORROW_TASK + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        long page = atomicLong.incrementAndGet();
        atomicLong.expire(Duration.ofSeconds(30));
        while (true) {
            try {
                var borrowRecords = borrowRecordService.page(new Page<>(page, 10)
                                , new LambdaQueryWrapper<BorrowRecord>()
                                        .in(BorrowRecord::getPledgeStatus, List.of(PledgeStatus.PROCESS))
                                        .eq(BorrowRecord::getUid, 1752243555481419778L)
                        )
                        .getRecords();
                if (CollectionUtils.isEmpty(borrowRecords)) {
                    break;
                }
                borrowRecords.forEach(borrowRecord -> {
                    final var newRecord = borrowService.calPledgeRate(borrowRecord, borrowRecord.getUid()
                            , borrowRecord.isAutoReplenishment(), false);

                    BigDecimal currencyPledgeRate = newRecord.getCurrencyPledgeRate();
                    BigDecimal warnPledgeRate = newRecord.getWarnPledgeRate();
                    BigDecimal lqPledgeRate = newRecord.getLqPledgeRate();
                    if (currencyPledgeRate.compareTo(warnPledgeRate) < 0) {
                        return;
                    }
                    // todo 通知用户
                    if (!borrowRecord.isAutoReplenishment() && currencyPledgeRate.compareTo(lqPledgeRate) < 0) {
                        return;
                    }
                    String lockKey = RedisLockConstants.LOCK_BORROW + borrowRecord.getUid();

                    // 自动补仓
                    if (currencyPledgeRate.compareTo(newRecord.getLqPledgeRate()) < 0) {
                        redissonClientTool.tryLock(lockKey, () -> borrowService.autoReplenishment(borrowRecord)
                                , ErrorCodeEnum.SYSTEM_BUSY, 30, TimeUnit.SECONDS, true);
                        return;
                    }

                    // 强制平仓（手动）
                    if (currencyPledgeRate.compareTo(newRecord.getAssureLqPledgeRate()) < 0) {
                        redissonClientTool.tryLock(lockKey, () -> borrowService.reduce(borrowRecord)
                                , ErrorCodeEnum.SYSTEM_BUSY, 30, TimeUnit.SECONDS, true);
                        return;
                    }

                    redissonClientTool.tryLock(lockKey, () -> borrowService.liquidate(borrowRecord)
                            , ErrorCodeEnum.SYSTEM_BUSY, 30, TimeUnit.SECONDS, true);

                });
                page = atomicLong.incrementAndGet();

            } catch (Exception e) {
                // 防止异常导致部分记录未计算
                e.printStackTrace();
                webHookService.dingTalkSend("借贷定时任务异常：" + key + "  page：" + page);
                break;
            }
        }
    }

//    @Scheduled(cron = "0 0 1/1 * * ?")
    public void interest() {
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.BORROW_TASK_INTEREST + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        long page = atomicLong.incrementAndGet();
        atomicLong.expire(Duration.ofSeconds(30));
        while (true) {
            try {
                var borrowRecords = borrowRecordService.page(new Page<>(page, 10)
                        , new LambdaQueryWrapper<BorrowRecord>()
                                .select(BorrowRecord::getId, BorrowRecord::getUid)
                                .in(BorrowRecord::getPledgeStatus, List.of(PledgeStatus.PROCESS))).getRecords();
                if (CollectionUtils.isEmpty(borrowRecords)) {
                    break;
                }
                borrowRecords.forEach(borrowRecord ->
                        borrowRecordCoinService.calInterest(borrowRecord.getUid(), borrowRecord.getId()));

                page = atomicLong.incrementAndGet();
            } catch (Exception e) {
                // 防止异常导致部分记录未计算
                webHookService.dingTalkSend("借贷定时利息任务异常：" + key + "  page：" + page);
            }
        }
    }

//    @Scheduled(cron = "0/10 * * * * ? ")
    public void entrust() {
        List<BorrowHedgeEntrust> borrowHedgeEntrusts = borrowHedgeEntrustService.list(new LambdaQueryWrapper<BorrowHedgeEntrust>()
                .in(BorrowHedgeEntrust::getHedgeStatus, List.of(HedgeStatus.WAIT, HedgeStatus.PROCESS)));

        borrowHedgeEntrusts.forEach(borrowHedgeEntrust -> {

            if (HedgeStatus.WAIT.equals(borrowHedgeEntrust.getHedgeStatus())) {
                borrowHedgeEntrustService.liquidate(borrowHedgeEntrust);
            }

            if (HedgeStatus.PROCESS.equals(borrowHedgeEntrust.getHedgeStatus())) {
                borrowHedgeEntrustService.liquidateStatus(borrowHedgeEntrust);
            }

        });

    }

}
