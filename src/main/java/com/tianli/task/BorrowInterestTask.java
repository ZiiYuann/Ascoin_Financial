package com.tianli.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.borrow.contant.BorrowOrderPledgeStatus;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.dao.BorrowInterestRecordMapper;
import com.tianli.borrow.dao.BorrowPledgeCoinConfigMapper;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.service.IBorrowInterestRecordService;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.tianli.common.RedisLockConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@Log4j2
public class BorrowInterestTask {
    
    @Autowired
    private IBorrowCoinOrderService borrowCoinOrderService;

    @Autowired
    private IBorrowCoinConfigService borrowCoinConfigService;

    @Autowired
    private IBorrowPledgeCoinConfigService borrowPledgeCoinConfigService;

    @Autowired
    private IBorrowInterestRecordService borrowInterestRecordService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 补偿数据
     */
    @PostConstruct
    public void compensationData(){
        LocalDateTime now = LocalDateTime.now();
        String hour = String.format("%s_%s_%s", now.getMonthValue(), now.getDayOfMonth(),now.getHour());
        String redisKey = RedisLockConstants.BORROW_INCOME_TASK + hour;
        Boolean hasKey = redisTemplate.hasKey(redisKey);
        if(Boolean.FALSE.equals(hasKey)){
            log.info("======借币计算利息补偿数据======");
            interestTasks(now);
        }
    }

    //3.添加定时任务（每小时执行一次）
    @Scheduled(cron = "0 0 * * * ?")
    public void interestTasks() {
        LocalDateTime now = LocalDateTime.now();
        interestTasks(now);
    }

    public void interestTasks(LocalDateTime now) {
        LocalDateTime dateTime = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0);
        String hour = String.format("%s_%s_%s", now.getMonthValue(), now.getDayOfMonth(),now.getHour());
        String redisKey = RedisLockConstants.BORROW_INCOME_TASK + hour;
        List<BorrowCoinConfig> borrowCoinConfigs = borrowCoinConfigService.list();
        List<BorrowPledgeCoinConfig> borrowPledgeCoinConfigs = borrowPledgeCoinConfigService.list();
        Map<String, BigDecimal> coinInterestRateMap = borrowCoinConfigs.stream().collect(Collectors.toMap(BorrowCoinConfig::getCoin, BorrowCoinConfig::getAnnualInterestRate));
        Map<String, BigDecimal> coinWarnPledgeRateMap = borrowPledgeCoinConfigs.stream().collect(Collectors.toMap(BorrowPledgeCoinConfig::getCoin, BorrowPledgeCoinConfig::getWarnPledgeRate));
        Map<String, BigDecimal> coinLiquidationPledgeRateMap = borrowPledgeCoinConfigs.stream().collect(Collectors.toMap(BorrowPledgeCoinConfig::getCoin, BorrowPledgeCoinConfig::getLiquidationPledgeRate));
        log.info("========执行计算利息定时任务========");
        while (true){
            long page = incr(redisKey,61L);
            List<BorrowCoinOrder> records = borrowCoinOrderService.page(new Page<>(page+1, 100),
                    new QueryWrapper<BorrowCoinOrder>().lambda()
                            .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL)).getRecords();
            if(CollUtil.isEmpty(records)){
                break;
            }
            records.forEach(record -> calculateInterest(record,coinInterestRateMap,coinWarnPledgeRateMap,coinLiquidationPledgeRateMap,dateTime));
        }
    }

    @Transactional
    public void calculateInterest(BorrowCoinOrder borrowCoinOrder,
                                  Map<String, BigDecimal> coinInterestRateMap,
                                  Map<String, BigDecimal> coinWarnPledgeRateMap,
                                  Map<String, BigDecimal> coinLiquidationPledgeRateMap,
                                  LocalDateTime dateTime){
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal cumulativeInterest = borrowCoinOrder.getCumulativeInterest();
        //总待还款
        BigDecimal totalWaitRepayAmount = borrowCoinOrder.calculateWaitRepay();
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        //计算每小时利息
        BigDecimal interestRate = coinInterestRateMap.get(borrowCoinOrder.getBorrowCoin());
        BigDecimal interest = (totalWaitRepayAmount.multiply(interestRate))
                .divide(new BigDecimal(365 * 24), 8, RoundingMode.UP);
        cumulativeInterest = cumulativeInterest.add(interest);
        waitRepayInterest = waitRepayInterest.add(interest);
        //计算质押率
        totalWaitRepayAmount = totalWaitRepayAmount.add(interest);
        BigDecimal pledgeRate = totalWaitRepayAmount.divide(pledgeAmount,8, RoundingMode.UP);
        //计算质押状态
        if(pledgeRate.compareTo(coinLiquidationPledgeRateMap.get(borrowCoinOrder.getPledgeCoin())) >= 0){
            borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.LIQUIDATION_PLEDGE);
        }else if(pledgeRate.compareTo(coinWarnPledgeRateMap.get(borrowCoinOrder.getPledgeCoin())) >= 0) {
            borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.WARN_PLEDGE);
        }
        //修改订单
        borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
        borrowCoinOrder.setCumulativeInterest(cumulativeInterest);
        borrowCoinOrder.setPledgeRate(pledgeRate);
        borrowCoinOrderService.updateById(borrowCoinOrder);

        //添加利息记录
        BorrowInterestRecord interestRecord = BorrowInterestRecord.builder()
                .interestAccrualTime(dateTime)
                .createTime(dateTime)
                .orderId(borrowCoinOrder.getId())
                .coin(borrowCoinOrder.getBorrowCoin())
                .waitRepayCapital(borrowCoinOrder.getWaitRepayCapital())
                .waitRepayInterest(borrowCoinOrder.getWaitRepayInterest())
                .interestAccrual(interest)
                .annualInterestRate(interestRate)
                .build();
        borrowInterestRecordService.save(interestRecord);
    }

    public Long incr(String key, long liveTime) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        long increment = entityIdCounter.getAndIncrement();
        if ( increment == 0 && liveTime > 0) {//初始设置过期时间
            entityIdCounter.expire(liveTime, TimeUnit.MINUTES);
        }
        return increment;
    }


}