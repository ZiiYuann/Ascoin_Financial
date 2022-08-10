package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    @PostMapping("/order/liquidation/{orderId}")
    @AdminPrivilege(and = Privilege.借币订单管理)
    public Result liquidation(@PathVariable Long orderId){
        borrowCoinOrderService.forcedLiquidation(orderId);
        return Result.success();
    }

    /**
     * 订单列表
     * @param
     * @return
     */
    @GetMapping("/order/record")
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
    public Result cumulativeInterestAmount(BorrowOrderQuery query){
        AmountVO amountVO = borrowCoinOrderService.cumulativeInterestAmount(query);
        return Result.success(amountVO);
    }


    /**
     * 累计数据
     * @param query
     * @return
     */
    @GetMapping("/order/cumulative/amount")
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
    public Result liquidationRecord(@PathVariable Long orderId){
        BorrowLiquidationRecordVO borrowLiquidationRecordVO = borrowCoinOrderService.liquidationRecord(orderId);
        return Result.success(borrowLiquidationRecordVO);
    }

    /**
     * 借贷数据统计
     * @return
     */
    @GetMapping("/order/statistics")
    @AdminPrivilege(and = Privilege.借币订单管理)
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
    @AdminPrivilege(and = Privilege.借币订单管理)
    public Result statisticsChart(@RequestParam BorrowStatisticsType statisticsType){
        List<BorrowOrderStatisticsChartVO> borrowOrderStatisticsChartVOS = borrowCoinOrderService.statisticsChart(statisticsType);
        return Result.success(borrowOrderStatisticsChartVOS);
    }
}
