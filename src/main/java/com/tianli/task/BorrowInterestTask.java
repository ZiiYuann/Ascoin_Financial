package com.tianli.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.dao.BorrowInterestRecordMapper;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.common.RedisLockConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@Log4j2
public class BorrowInterestTask {
    
    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private BorrowCoinConfigMapper borrowCoinConfigMapper;

    @Autowired
    private BorrowInterestRecordMapper borrowInterestRecordMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //3.添加定时任务（每小时执行一次）
    @Scheduled(cron = "0 0 * * * ?")
    public void interestTasks() {

        LocalDateTime now = LocalDateTime.now();
        String hour = String.format("%s_%s_%s", now.getMonthValue(), now.getDayOfMonth(),now.getHour());
        String redisKey = RedisLockConstants.BORROW_INCOME_TASK + hour;
        BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisKey);
        operation.setIfAbsent(0, 30, TimeUnit.MINUTES);

        log.info("========执行计算利息定时任务========");
        while (true){
            Long page = operation.increment();

            List<BorrowCoinOrder> records;
            if(page == null){
                records = new ArrayList<>();
            }else {
                records = borrowCoinOrderMapper.selectPage(new Page<>(page, 100),
                        new QueryWrapper<BorrowCoinOrder>().lambda()
                                .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL)).getRecords();
            }
            if(CollUtil.isEmpty(records)){
                break;
            }
            records.forEach(record -> calculateInterest(record,now));
        }
    }

    @Transactional
    public void calculateInterest(BorrowCoinOrder borrowCoinOrder, LocalDateTime now){
        //判断是否已计息
        Integer count = borrowInterestRecordMapper.selectCountByOrderIdAndTime(borrowCoinOrder.getId(), now);
        if(count > 0){
            return;
        }

        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal cumulativeInterest = borrowCoinOrder.getCumulativeInterest();
        //总待还款
        BigDecimal totalWaitRepayAmount = borrowCoinOrder.calculateWaitRepay();
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        //计算每小时利息
        BigDecimal interestRate = borrowCoinConfigMapper.getAnnualInterestRateByCoin(borrowCoinOrder.getBorrowCoin());
        BigDecimal interest = (totalWaitRepayAmount.multiply(interestRate))
                .divide(new BigDecimal(365 * 24), 8, RoundingMode.UP);
        cumulativeInterest = cumulativeInterest.add(interest);
        waitRepayInterest = waitRepayInterest.add(interest);
        //计算质押率
        totalWaitRepayAmount = totalWaitRepayAmount.add(interest);
        BigDecimal pledgeRate = totalWaitRepayAmount.divide(pledgeAmount,8, RoundingMode.UP);

        //修改订单
        borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
        borrowCoinOrder.setCumulativeInterest(cumulativeInterest);
        borrowCoinOrder.setPledgeRate(pledgeRate);
        borrowCoinOrderMapper.updateById(borrowCoinOrder);

        //添加利息记录
        BorrowInterestRecord interestRecord = BorrowInterestRecord.builder()
                .interestAccrualTime(now)
                .createTime(now)
                .orderId(borrowCoinOrder.getId())
                .coin(borrowCoinOrder.getBorrowCoin())
                .waitRepayCapital(borrowCoinOrder.getWaitRepayCapital())
                .waitRepayInterest(borrowCoinOrder.getWaitRepayInterest())
                .interestAccrual(interest)
                .annualInterestRate(interestRate)
                .build();
        borrowInterestRecordMapper.insert(interestRecord);
    }
}