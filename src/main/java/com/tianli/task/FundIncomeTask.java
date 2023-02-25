package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.MoreObjects;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.afund.contant.FundCycle;
import com.tianli.product.afund.contant.FundIncomeStatus;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.enums.FundRecordStatus;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.management.query.FundIncomeCompensateQuery;
import com.tianli.product.service.FinancialProductService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.time.TimeTool;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
@Log4j2
public class FundIncomeTask {


    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 1 0 1/1 * ? ")
    public void incomeTasks() {
        log.info("========执行基金计算利息定时任务========");
        LocalDateTime now = LocalDateTime.now();
        String day = String.format("%s_%s", now.getMonthValue(), now.getDayOfMonth());
        String redisKey = RedisLockConstants.FUND_INCOME_TASK + day;
        long page = incr(redisKey, 60L);
        List<FundRecord> records = fundRecordService.page(new Page<>(page, 100)
                        , new QueryWrapper<FundRecord>().lambda().eq(FundRecord::getStatus, FundRecordStatus.PROCESS))
                .getRecords();

        var bean = ApplicationContextTool.getBean(FundIncomeTask.class);
        var fundIncomeTask = Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);

        records.forEach(fundRecord -> {
            RLock lock = redissonClient.getLock(RedisLockConstants.FUND_UPDATE_LOCK + fundRecord.getId());
            try {
                lock.lock();
                fundIncomeTask.calculateIncome(fundRecord, now);
            } finally {
                lock.unlock();
            }

        });
    }

    @Transactional
    public void calculateIncome(FundRecord fundRecord, LocalDateTime now) {
        LocalDateTime todayZero = TimeTool.minDay(now);
        LocalDateTime createTime = fundRecord.getCreateTime();
        // t + 4 是开始记录利息的时间  1号12点申购 5号开始计算利息 6号开始发5号利息
        LocalDateTime startIncomeTime = TimeTool.minDay(createTime).plusDays(4);
        long cha;
        // 四天后开始计息 createTime 25号 createTime.toLocalDate().plusDays(1) 26号 26 27 28 等待 29开始 30发放
        //
        if (createTime.plusDays(1).toLocalDate().until(now, ChronoUnit.DAYS) >= FundCycle.interestCalculationCycle) {
            if (BigDecimal.ZERO.compareTo(fundRecord.getHoldAmount()) == 0) {
                return;
            }
            BigDecimal dailyIncome = financialProductService.exceptDailyIncome(
                    fundRecord.getHoldAmount(), fundRecord.getRate(), 365).getExpectIncome();
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
                    // 收益的记录时间为计息当日的时间
                    .createTime(todayZero.plusDays(-1))
                    .build();
            fundIncomeRecordService.save(incomeRecord);

            //累计收益
            fundRecord.setCumulativeIncomeAmount(fundRecord.getCumulativeIncomeAmount().add(dailyIncome));
            // 增加待发放利息
            fundRecord.setWaitIncomeAmount(fundRecord.getWaitIncomeAmount().add(dailyIncome));

        }

        if (startIncomeTime.compareTo(todayZero) < 0
                && (cha = startIncomeTime.until(todayZero, ChronoUnit.DAYS)) >= 7
                && cha % 7 == 0) {
            // 查询已经计算的收益信息
            List<FundIncomeRecord> fundIncomeRecords = fundIncomeRecordService.list(new QueryWrapper<FundIncomeRecord>().lambda()
                    .eq(FundIncomeRecord::getFundId, fundRecord.getId())
                    .eq(FundIncomeRecord::getStatus, FundIncomeStatus.calculated)
                    .between(FundIncomeRecord::getCreateTime, todayZero.plusDays(-7), todayZero.plusDays(-1)));
            fundIncomeRecords.forEach(fundIncomeRecord -> {
                fundIncomeRecord.setStatus(FundIncomeStatus.wait_audit);
                fundIncomeRecordService.updateById(fundIncomeRecord);
            });

            // 发送消息
            String msg = WebHookTemplate.fundIncome(fundRecord.getUid(), fundRecord.getHoldAmount()
                    , fundRecord.getWaitIncomeAmount(), fundRecord.getCoin());
            webHookService.fundSend(msg);
        }

        fundRecordService.updateById(fundRecord);
    }

    @Transactional
    public void incomeCompensate(FundIncomeCompensateQuery query) {
        LocalDateTime todayZero = TimeTool.minDay(query.getNow());
        var fundRecord = fundRecordService.getById(query.getFundId());
        LocalDateTime createTime = fundRecord.getCreateTime();

        BigDecimal holdAmount = MoreObjects.firstNonNull(query.getAmount(), fundRecord.getHoldAmount());
        //四天后开始计息
        if (createTime.toLocalDate().until(query.getNow(), ChronoUnit.DAYS) >= FundCycle.interestCalculationCycle) {
            if (BigDecimal.ZERO.compareTo(holdAmount) == 0) {
                return;
            }
            BigDecimal dailyIncome = financialProductService.exceptDailyIncome(holdAmount, fundRecord.getRate(), 365).getExpectIncome();
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
                    .status(query.getStatus())
                    // 收益的记录时间为计息当日的时间
                    .createTime(todayZero.plusDays(-1))
                    .build();
            fundIncomeRecordService.save(incomeRecord);

            //累计收益
            fundRecord.setCumulativeIncomeAmount(fundRecord.getCumulativeIncomeAmount().add(dailyIncome));
            // 增加待发放利息
            fundRecord.setWaitIncomeAmount(fundRecord.getWaitIncomeAmount().add(dailyIncome));
            webHookService.dingTalkSend("补偿利息 fundId:" + query.getFundId() + "  金额:" + dailyIncome.toPlainString());
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
