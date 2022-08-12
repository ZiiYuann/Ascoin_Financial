package com.tianli.borrow.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.entity.BorrowOrderNumDaily;
import com.tianli.borrow.dao.BorrowOrderNumDailyMapper;
import com.tianli.borrow.service.IBorrowOrderNumDailyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import com.tianli.common.lock.RedisLock;
import com.tianli.tool.time.TimeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 计息中订单每日统计 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-01
 */
@Service
public class BorrowOrderNumDailyServiceImpl extends ServiceImpl<BorrowOrderNumDailyMapper, BorrowOrderNumDaily> implements IBorrowOrderNumDailyService {

    @Autowired
    private BorrowOrderNumDailyMapper borrowOrderNumDailyMapper;

    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private RedisLock redisLock;

    @PostConstruct
    public void addData(){
        redisLock.lock("BorrowOrderNumDailyServiceImpl:addData",10L, TimeUnit.SECONDS);
        LocalDate localDate2 = LocalDate.of(2022, 8, 11);
        BorrowOrderNumDaily borrowOrderNumDaily2 = getByDate(localDate2);
        if(Objects.isNull(borrowOrderNumDaily2)){
            borrowOrderNumDaily2 = BorrowOrderNumDaily.builder()
                    .orderNum(8)
                    .statisticalDate(localDate2).build();
            borrowOrderNumDailyMapper.insert(borrowOrderNumDaily2);
        }
        LocalDate localDate = LocalDate.of(2022, 8, 12);
        BorrowOrderNumDaily borrowOrderNumDaily = getByDate(localDate);
        if(Objects.isNull(borrowOrderNumDaily)){
            borrowOrderNumDaily = BorrowOrderNumDaily.builder()
                    .orderNum(8)
                    .statisticalDate(localDate).build();
            borrowOrderNumDailyMapper.insert(borrowOrderNumDaily);
        }

    }

    @Override
    public void statisticalOrderNum() {
        LocalDate now = LocalDate.now();
        BorrowOrderNumDaily borrowOrderNumDaily = getByDate(now);
        Integer count = borrowCoinOrderMapper.selectCountByStatusAndTime(BorrowOrderStatus.INTEREST_ACCRUAL,now);
        if(Objects.nonNull(borrowOrderNumDaily)){
            borrowOrderNumDaily.setOrderNum(count);
        } else {
            borrowOrderNumDaily = BorrowOrderNumDaily.builder()
                    .orderNum(count)
                    .statisticalDate(LocalDate.now()).build();
        }
        this.saveOrUpdate(borrowOrderNumDaily);
    }

    @Override
    public void increaseNum() {
        BorrowOrderNumDaily borrowOrderNumDaily = getByDate(LocalDate.now());
        if(Objects.isNull(borrowOrderNumDaily)){
            statisticalOrderNum();
        }else {
            borrowOrderNumDailyMapper.increaseNum(borrowOrderNumDaily.getId());
        }

    }

    @Override
    public void reduceNum() {
        BorrowOrderNumDaily borrowOrderNumDaily = getByDate(LocalDate.now());
        if(Objects.isNull(borrowOrderNumDaily)){
            statisticalOrderNum();
        }else {
            borrowOrderNumDailyMapper.reduceNum(borrowOrderNumDaily.getId());
        }

    }

    @Override
    public BorrowOrderNumDaily getByDate(LocalDate localDate) {
        return borrowOrderNumDailyMapper.selectOne(new QueryWrapper<BorrowOrderNumDaily>().lambda()
                .eq(BorrowOrderNumDaily::getStatisticalDate,localDate));
    }

    @Override
    public Integer getCount(Date startDate, Date endDate) {
        return borrowOrderNumDailyMapper.selectCountByDate(TimeTool.dateToLocalDate(startDate),TimeTool.dateToLocalDate(endDate));
    }

    @Override
    public List<BorrowOrderStatisticsChartVO> selectTotalChart(DateTime beginOfDay) {
        return borrowOrderNumDailyMapper.selectTotalChartByTime(TimeTool.dateToLocalDate(beginOfDay));
    }
}
