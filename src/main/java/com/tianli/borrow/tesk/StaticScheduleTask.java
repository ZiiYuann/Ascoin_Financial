package com.tianli.borrow.tesk;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.dao.BorrowInterestRecordMapper;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.common.blockchain.CurrencyCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
public class StaticScheduleTask {
    
    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private BorrowCoinConfigMapper borrowCoinConfigMapper;

    @Autowired
    private BorrowInterestRecordMapper borrowInterestRecordMapper;

    //3.添加定时任务
    //@Scheduled(cron = "0 0 * * * ?")
    private void configureTasks() {
        Date now = new Date();
        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));

        BigDecimal annualInterestRate = coinConfig.getAnnualInterestRate();

        BorrowInterestRecord interestRecord = BorrowInterestRecord.builder()
                .interestAccrualTime(now)
                .createTime(now).build();

        int page = 1;
        while (true){
            Page<BorrowCoinOrder> borrowCoinOrderPage = borrowCoinOrderMapper.selectPage(new Page<>(page, 20),
                    new QueryWrapper<BorrowCoinOrder>().lambda()
                            .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL));
            List<BorrowCoinOrder> records = borrowCoinOrderPage.getRecords();
            if(CollUtil.isEmpty(records)){
                break;
            }
            
            records.forEach(borrowCoinOrder -> {
                Date createTime = borrowCoinOrder.getCreateTime();
                //相差超过一个小时开始计利息
                if(DateUtil.between(now,createTime, DateUnit.HOUR) > 1L){
                    BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
                    BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
                    BigDecimal cumulativeInterest = borrowCoinOrder.getCumulativeInterest();
                    BigDecimal totalWaitRepayAmount = waitRepayCapital.add(waitRepayInterest);
                    BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
                    //计算每小时利息
                    BigDecimal interest = (totalWaitRepayAmount.multiply(annualInterestRate))
                            .divide(new BigDecimal(365 * 24), 8, RoundingMode.UP);

                    waitRepayInterest = waitRepayInterest.add(interest);
                    //计算质押率
                    BigDecimal pledgeRate = (totalWaitRepayAmount.add(interest)).divide(pledgeAmount,8, RoundingMode.UP);
                    totalWaitRepayAmount = cumulativeInterest.add(interest);
                    //修改订单
                    borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
                    borrowCoinOrder.setCumulativeInterest(totalWaitRepayAmount);
                    borrowCoinOrder.setPledgeRate(pledgeRate);
                    borrowCoinOrderMapper.updateById(borrowCoinOrder);

                    interestRecord.setOrderId(borrowCoinOrder.getId());
                    interestRecord.setCoin(borrowCoinOrder.getBorrowCoin());
                    interestRecord.setWaitRepayCapital(borrowCoinOrder.getWaitRepayCapital());
                    interestRecord.setWaitRepayInterest(borrowCoinOrder.getWaitRepayInterest());
                    interestRecord.setInterestAccrual(interest);
                    //添加利息记录
                    borrowInterestRecordMapper.insert(interestRecord);
                }
                
            });
            page++;
        }
        
        
        System.err.println("执行静态定时任务时间: " + LocalDateTime.now());
    }
}