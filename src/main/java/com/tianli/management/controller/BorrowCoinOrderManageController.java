package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.enums.BorrowOrderStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/management/borrow")
public class BorrowCoinOrderManageController {

    @Autowired
    private IBorrowCoinOrderService borrowCoinOrderService;

    /**
     * 强制平仓
     * @param orderId
     * @return
     */
    @PostMapping("/order/liquidation")
    public Result liquidation(@RequestBody Long orderId){
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
        IPage<BorrowCoinOrderVO> page = borrowCoinOrderService.pageList(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 质押记录
     * @param
     * @return
     */
    @GetMapping("/order/pledge/record")
    public Result pledgeRecord(PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query){
        IPage<BorrowPledgeRecordVO> page = borrowCoinOrderService.pledgeRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 利息记录
     * @param
     * @return
     */
    @GetMapping("/order/interest/record")
    public Result interestRecord(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query){
        IPage<BorrowInterestRecordVO> page = borrowCoinOrderService.interestRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 还款记录
     * @param
     * @return
     */
    @GetMapping("/order/repay/record")
    public Result repayRecord(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query){
        IPage<BorrowRepayRecordVO> borrowRepayRecordVOS = borrowCoinOrderService.repayRecord(pageQuery,query);
        return Result.success(borrowRepayRecordVOS);
    }

    /**
     * 借贷数据统计
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/order/statistics")
    public Result statistics(Date startTime,Date endTime){
        BorrowOrderStatisticsVO statistics = borrowCoinOrderService.statistics(startTime, endTime);
        return Result.success(statistics);
    }

    /**
     * 借贷数据统计图表
     * @param statisticsType
     * @return
     */
    @GetMapping("/order/statistics/chart")
    public Result statisticsChart(BorrowOrderStatisticsType statisticsType){
        List<BorrowOrderStatisticsChartVO> borrowOrderStatisticsChartVOS = borrowCoinOrderService.statisticsChart(statisticsType);
        return Result.success(borrowOrderStatisticsChartVOS);
    }
}
