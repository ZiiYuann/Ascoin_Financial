package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.RedisLockConstants;
import com.tianli.fund.contant.FundCycle;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.enums.FundRecordStatus;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@EnableScheduling
@Log4j2
public class FundIncomeTask {


    @Autowired
    private IFundRecordService fundRecordService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Autowired
    private RedissonClient redissonClient;

        @Scheduled(cron = "0 0 0 1/1 * ? ")
//    @Scheduled(cron = "0 0/1 * * * ?")
    public void incomeTasks() {
        log.info("========执行基金计算利息定时任务========");
        LocalDateTime now = LocalDateTime.now();
        String day = String.format("%s_%s", now.getMonthValue(), now.getDayOfMonth());
        String redisKey = RedisLockConstants.FUND_INCOME_TASK + day;
        long page = incr(redisKey, 60L);
        List<FundRecord> records = fundRecordService.page(new Page<>(page, 100)
                        , new QueryWrapper<FundRecord>().lambda().eq(FundRecord::getStatus, FundRecordStatus.PROCESS))
                .getRecords();
        records.forEach(fundRecord -> {
            RLock lock = redissonClient.getLock(RedisLockConstants.FUND_UPDATE_LOCK + fundRecord.getId());
            try {
                lock.lock();
                calculateIncome(fundRecord, now);
            } finally {
                lock.unlock();
            }

        });
    }

    @Transactional
    public void calculateIncome(FundRecord fundRecord, LocalDateTime now) {
        //收益记录状态改变
        List<FundIncomeRecord> fundIncomeRecords = fundIncomeRecordService.list(new QueryWrapper<FundIncomeRecord>().lambda()
                .eq(FundIncomeRecord::getFundId, fundRecord.getId())
                .eq(FundIncomeRecord::getStatus, FundIncomeStatus.calculated));
        fundIncomeRecords.forEach(fundIncomeRecord -> {
            LocalDateTime fundIncomeRecordCreateTime = fundIncomeRecord.getCreateTime();
            if (fundIncomeRecordCreateTime.until(now, ChronoUnit.DAYS) >= FundCycle.interestAuditCycle) {
                fundIncomeRecord.setStatus(FundIncomeStatus.wait_audit);
                fundIncomeRecordService.updateById(fundIncomeRecord);
            }
            //待发放收益
            fundRecord.setWaitIncomeAmount(fundRecord.getIncomeAmount().add(fundIncomeRecord.getInterestAmount()));
        });

        LocalDateTime createTime = fundRecord.getCreateTime();
        //四天后开始计息
        if (createTime.toLocalDate().plusDays(1).until(now, ChronoUnit.DAYS) >= FundCycle.interestCalculationCycle) {
            BigDecimal dailyIncome = fundRecordService.dailyIncome(fundRecord.getHoldAmount(), fundRecord.getRate());
            //收益记录
            FundIncomeRecord incomeRecord = FundIncomeRecord.builder()
                    .uid(fundRecord.getUid())
                    .fundId(fundRecord.getId())
                    .productId(fundRecord.getProductId())
                    .productName(fundRecord.getProductName())
                    .coin(fundRecord.getCoin())
                    .rate(fundRecord.getRate())
                    .holdAmount(fundRecord.getHoldAmount())
                    .interestAmount(dailyIncome)
                    .status(FundIncomeStatus.calculated)
                    .createTime(now)
                    .build();
            fundIncomeRecordService.save(incomeRecord);

            //累计收益
            fundRecord.setCumulativeIncomeAmount(fundRecord.getCumulativeIncomeAmount().add(dailyIncome));

        }
        fundRecordService.updateById(fundRecord);
    }

    public Long incr(String key, long liveTime) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        long increment = atomicLong.incrementAndGet();
        if (increment == 1 && liveTime > 0) {//初始设置过期时间
            atomicLong.expire(Duration.ofMinutes(liveTime));
        }
        return increment;
    }

}
