package com.tianli.borrow.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
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
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    /**
     * 借币主页面
     * @return
     */
    @GetMapping("/main/page")
    public Result main(){
        BorrowCoinMainPageVO borrowCoinMainPageVO = borrowCoinOrderService.mainPage();
        return Result.success(borrowCoinMainPageVO);
    }

    /**
     * 借币订单历史
     * @param pageQuery
     * @return
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
        IPage<BorrowCoinOrderVO> borrowCoinOrderVOIPage = borrowCoinOrderService.pageList(pageQuery, query);
        return Result.success(borrowCoinOrderVOIPage);
    }

    /**
     * 借币申请页面
     * @return
     */
    @GetMapping("/apply/page")
    public Result applyPage(CurrencyCoin coin){
        BorrowApplePageVO config = borrowCoinOrderService.applyPage(coin);
        return Result.success(config);
    }

    /**
     * 借币
     * @param bo
     * @return
     */
    @PostMapping("/order")
    public Result coin(@RequestBody @Valid BorrowOrderBO bo){
        borrowCoinOrderService.borrowCoin(bo);
        return Result.success();
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/order/{orderId}")
    public Result orderInfo(@PathVariable Long orderId){
        BorrowCoinOrderVO info = borrowCoinOrderService.info(orderId);
        return Result.success(info);
    }

    /**
     * 借款记录
     * @param orderId
     * @return
     */
    @GetMapping("/order/borrow/record/{orderId}")
    public Result orderBorrowRecord(@PathVariable Long orderId){
        BorrowRecordVO recordVO = borrowCoinOrderService.borrowRecord(orderId);
        return Result.success(recordVO);
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
     * 还款记录
     * @param
     * @return
     */
    @GetMapping("/order/repay/record")
    public Result repayRecord(PageQuery<BorrowRepayRecord> pageQuery,@Valid BorrowRepayQuery query){
        IPage<BorrowRepayRecordVO> borrowRepayRecordVOS = borrowCoinOrderService.repayRecord(pageQuery,query);
        return Result.success(borrowRepayRecordVOS);
    }

    /**
     * 还款页面
     * @param orderId
     * @param coin
     * @return
     */
    @GetMapping("/order/repay/page")
    public Result repayPage(Long orderId,CurrencyCoin coin){
        BorrowRepayPageVO borrowRepayPageVO = borrowCoinOrderService.repayPage(orderId, coin);
        return Result.success(borrowRepayPageVO);
    }

    /**
     * 还款
     * @param bo
     * @return
     */
    @PostMapping("/order/repay")
    public Result orderRepay(@RequestBody @Valid BorrowOrderRepayBO bo){
        borrowCoinOrderService.orderRepay(bo);
        return Result.success();
    }

    /**
     * 调整质押率
     * @param bo
     * @return
     */
    @PostMapping("/order/adjust/pledge")
    public Result adjustPledge(@RequestBody @Valid AdjustPledgeBO bo){
        borrowCoinOrderService.adjustPledge(bo);
        return Result.success();
    }

}

