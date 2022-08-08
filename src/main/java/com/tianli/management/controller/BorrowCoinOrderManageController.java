package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.enums.BorrowStatisticsChartDay;
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.annotation.NoOperation;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/management/borrow")
public class BorrowCoinOrderManageController {

    @Autowired
    private IBorrowCoinOrderService borrowCoinOrderService;
    @Autowired
    private RedisLock redisLock;

    /**
     * 强制平仓
     * @param orderId
     * @return
     */
    @PostMapping("/order/liquidation/{orderId}")
    public Result liquidation(@PathVariable Long orderId){
        //防重复提交锁
        redisLock.lock(RedisLockConstants.BORROW_ORDER_CHANGE_LOCK+orderId,5L, TimeUnit.SECONDS);
        borrowCoinOrderService.forcedLiquidation(orderId);
        return Result.success();
    }

    /**
     * 订单列表
     * @param
     * @return
     */
    @GetMapping("/order/record")
    public Result pledgeRecord(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query){
        query.setOrderColumn("borrow_time");
        IPage<BorrowCoinOrderVO> page = borrowCoinOrderService.pageList(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 订单利息合计
     * @param
     * @return
     */
    @GetMapping("/order/cumulative/interest/amount")
    public Result cumulativeInterestAmount(BorrowOrderQuery query){
        AmountVO amountVO = borrowCoinOrderService.cumulativeInterestAmount(query);
        return Result.success(amountVO);
    }



    @GetMapping("/order/cumulative/amount")
    public Result cumulativeAmount(BorrowOrderQuery query){
        BorrowOrderAmountVO borrowOrderAmountVO = borrowCoinOrderService.cumulativeAmount(query);
        return Result.success(borrowOrderAmountVO);
    }

    /**
     * 质押记录
     * @param
     * @return
     */
    @GetMapping("/order/pledge/record")
    public Result pledgeRecord(PageQuery<BorrowPledgeRecord> pageQuery,@Valid BorrowPledgeRecordQuery query){
        IPage<BorrowPledgeRecordVO> page = borrowCoinOrderService.pledgeRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 利息记录
     * @param
     * @return
     */
    @GetMapping("/order/interest/record")
    public Result interestRecord(PageQuery<BorrowInterestRecord> pageQuery,@Valid BorrowInterestRecordQuery query){
        IPage<BorrowInterestRecordVO> page = borrowCoinOrderService.interestRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 利息总额
     * @param query
     * @return
     */
    @GetMapping("/order/interest/amount")
    public Result interestAmount( @Valid BorrowInterestRecordQuery query){
        AmountVO amountVO = borrowCoinOrderService.interestAmount(query);
        return Result.success(amountVO);
    }

    /**
     * 还款记录
     * @param
     * @return
     */
    @GetMapping("/order/repay/record")
    public Result repayRecord(PageQuery<BorrowRepayRecord> pageQuery, @Valid BorrowRepayQuery query){
        IPage<BorrowRepayRecordVO> borrowRepayRecordVOS = borrowCoinOrderService.repayRecord(pageQuery,query);
        return Result.success(borrowRepayRecordVOS);
    }

    /**
     * 还款总额
     * @param query
     * @return
     */
    @GetMapping("/order/repay/amount")
    public Result repayAmount( @Valid BorrowRepayQuery query){
        AmountVO amountVO = borrowCoinOrderService.repayAmount(query);
        return Result.success(amountVO);
    }

    /**
     * 平仓记录
     * @param orderId
     * @return
     */
    @GetMapping("/order/liquidation/record/{orderId}")
    public Result liquidationRecord(@PathVariable Long orderId){
        BorrowLiquidationRecordVO borrowLiquidationRecordVO = borrowCoinOrderService.liquidationRecord(orderId);
        return Result.success(borrowLiquidationRecordVO);
    }

    /**
     * 借贷数据统计
     * @return
     */
    @GetMapping("/order/statistics")
    public Result statistics(){
        BorrowOrderStatisticsVO statistics = borrowCoinOrderService.statistics();
        return Result.success(statistics);
    }

    /**
     * 借贷数据统计图表
     * @param statisticsType
     * @return
     */
    @GetMapping("/order/statistics/chart")
    public Result statisticsChart(@RequestParam BorrowStatisticsType statisticsType){
        List<BorrowOrderStatisticsChartVO> borrowOrderStatisticsChartVOS = borrowCoinOrderService.statisticsChart(statisticsType);
        return Result.success(borrowOrderStatisticsChartVOS);
    }
}
