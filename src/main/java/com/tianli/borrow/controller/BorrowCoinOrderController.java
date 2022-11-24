package com.tianli.borrow.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.annotation.NoOperation;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 借币订单 前端控制器
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@RestController
@RequestMapping("/borrow")
public class BorrowCoinOrderController {

    @Autowired
    private RequestInitService requestInitService;

    @Autowired
    private IBorrowCoinOrderService borrowCoinOrderService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 借币主页面
     */
    @GetMapping("/main/page")
    public Result main() {
        BorrowCoinMainPageVO borrowCoinMainPageVO = borrowCoinOrderService.mainPage();
        return Result.success(borrowCoinMainPageVO);
    }

    /**
     * 借币订单历史
     */
    @GetMapping("/order/history")
    public Result history(PageQuery<BorrowCoinOrder> pageQuery){
        Long uid = requestInitService.uid();
        BorrowOrderQuery query = new BorrowOrderQuery();
        Set<Integer> status = new HashSet<>(2);
        status.add(BorrowOrderStatus.SUCCESSFUL_REPAYMENT);
        status.add(BorrowOrderStatus.FORCED_LIQUIDATION);
        query.setStatus(status);
        query.setUid(uid);
        query.setOrderColumn("settlement_time");
        IPage<BorrowCoinOrderVO> borrowCoinOrderVOIPage = borrowCoinOrderService.pageList(pageQuery, query);
        return Result.success(borrowCoinOrderVOIPage);
    }

    /**
     * 借币申请页面
     */
    @GetMapping("/apply/page")
    public Result applyPage(String coin){
        BorrowApplePageVO config = borrowCoinOrderService.applyPage(coin);
        return Result.success(config);
    }

    /**
     * 借币
     */
    @PostMapping("/order")
    public Result order(@RequestBody @Valid BorrowOrderBO bo){
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.BORROW_ORDER_CREATE_LOCK+uid);
        try {
            lock.lock();
            borrowCoinOrderService.borrowCoin(bo);
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/{orderId}")
    public Result orderInfo(@PathVariable Long orderId){
        BorrowCoinOrderVO info = borrowCoinOrderService.info(orderId);
        return Result.success(info);
    }

    /**
     * 借款记录
     */
    @GetMapping("/order/borrow/record/{orderId}")
    public Result orderBorrowRecord(@PathVariable Long orderId){
        BorrowRecordVO recordVO = borrowCoinOrderService.borrowRecord(orderId);
        return Result.success(recordVO);
    }

    /**
     * 质押记录
     */
    @GetMapping("/order/pledge/record")
    public Result pledgeRecord(PageQuery<BorrowPledgeRecord> pageQuery,@Valid BorrowPledgeRecordQuery query){
        IPage<BorrowPledgeRecordVO> page = borrowCoinOrderService.pledgeRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 利息记录
     */
    @GetMapping("/order/interest/record")
    public Result interestRecord(PageQuery<BorrowInterestRecord> pageQuery,@Valid BorrowInterestRecordQuery query){
        IPage<BorrowInterestRecordVO> page = borrowCoinOrderService.interestRecord(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 还款记录
     */
    @GetMapping("/order/repay/record")
    public Result repayRecord(PageQuery<BorrowRepayRecord> pageQuery,@Valid BorrowRepayQuery query){
        IPage<BorrowRepayRecordVO> borrowRepayRecordVOS = borrowCoinOrderService.repayRecord(pageQuery,query);
        return Result.success(borrowRepayRecordVOS);
    }

    /**
     * 还款页面
     */
    @GetMapping("/order/repay/page")
    public Result repayPage(@RequestParam Long orderId,
                            @RequestParam(defaultValue = "0.0") BigDecimal repayAmount,
                            @RequestParam String coin){
        BorrowRepayPageVO borrowRepayPageVO = borrowCoinOrderService.repayPage(orderId,repayAmount, coin);
        return Result.success(borrowRepayPageVO);
    }

    /**
     * 还款
     */
    @PostMapping("/order/repay")
    @NoOperation
    public Result orderRepay(@RequestBody @Valid BorrowOrderRepayBO bo){
        RLock lock = redissonClient.getLock(RedisLockConstants.BORROW_ORDER_UPDATE_LOCK + bo.getOrderId());
        try {
            lock.lock();
            borrowCoinOrderService.orderRepay(bo);
        }finally {
            lock.unlock();
        }
        return Result.success();
    }

    /**
     * 调整质押页面
     */
    @GetMapping("/order/adjust/page")
    public Result adjustPage(@RequestParam Long orderId,
                             @RequestParam(defaultValue = "2") Integer pledgeType,
                             @RequestParam(defaultValue = "0") BigDecimal adjustAmount,
                             @RequestParam String coin){
        BorrowAdjustPageVO borrowAdjustPageVO = borrowCoinOrderService.adjustPage(orderId,pledgeType,adjustAmount, coin);
        return Result.success(borrowAdjustPageVO);
    }

    /**
     * 调整质押率
     */
    @PostMapping("/order/adjust/pledge")
    public Result adjustPledge(@RequestBody @Valid AdjustPledgeBO bo){
        RLock lock = redissonClient.getLock(RedisLockConstants.BORROW_ORDER_UPDATE_LOCK + bo.getOrderId());
        try {
            lock.lock();
            borrowCoinOrderService.adjustPledge(bo);
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

}

