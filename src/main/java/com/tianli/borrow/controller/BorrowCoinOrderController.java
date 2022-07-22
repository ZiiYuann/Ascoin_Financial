package com.tianli.borrow.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dto.BorrowCoinOrderDTO;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.query.BorrowCoinOrderQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private IBorrowCoinOrderService borrowCoinOrderService;

    /**
     * 借币主页面
     * @return
     */
    @GetMapping("/main/info")
    public Result main(){
        BorrowCoinMainPageVO borrowCoinMainPageVO = borrowCoinOrderService.mainPage();
        return Result.success(borrowCoinMainPageVO);
    }

    /**
     * 借币订单历史
     * @param pageQuery
     * @return
     */
    @GetMapping("/order/history/list")
    public Result history(PageQuery<BorrowCoinOrder> pageQuery){
        BorrowCoinOrderQuery query = new BorrowCoinOrderQuery();
        Integer[] status = {BorrowOrderStatus.SUCCESSFUL_REPAYMENT,BorrowOrderStatus.FORCED_LIQUIDATION};
        query.setOrderStatus(status);
        IPage<BorrowCoinOrderVO> borrowCoinOrderVOIPage = borrowCoinOrderService.pageList(pageQuery, query);
        return Result.success(borrowCoinOrderVOIPage);
    }

    /**
     * 借币配置信息
     * @return
     */
    @GetMapping("/config/info")
    public Result config(){
        BorrowCoinConfigVO config = borrowCoinOrderService.config();
        return Result.success(config);
    }

    /**
     * 借币
     * @param borrowCoinOrderDTO
     * @return
     */
    @PostMapping("/order/borrow")
    public Result coin(@RequestBody BorrowCoinOrderDTO borrowCoinOrderDTO){
        borrowCoinOrderService.borrowCoin(borrowCoinOrderDTO);
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
     * @param orderId
     * @return
     */
    @GetMapping("/order/pledge/record/{orderId}")
    public Result pledgeRecord(@PathVariable Long orderId){
        List<BorrowPledgeRecordVO> borrowPledgeRecordVOS = borrowCoinOrderService.pledgeRecord(orderId);
        return Result.success(borrowPledgeRecordVOS);
    }

    /**
     * 利息记录
     * @param orderId
     * @return
     */
    @GetMapping("/order/interest/record/{orderId}")
    public Result interestRecord(@PathVariable Long orderId){
        List<BorrowInterestRecordVO> borrowInterestRecordVOS = borrowCoinOrderService.interestRecord(orderId);
        return Result.success(borrowInterestRecordVOS);
    }

    /**
     * 还款记录
     * @param orderId
     * @return
     */
    @GetMapping("/order/repay/record/{orderId}")
    public Result repayRecord(@PathVariable Long orderId){
        List<BorrowInterestRecordVO> borrowInterestRecordVOS = borrowCoinOrderService.interestRecord(orderId);
        return Result.success(borrowInterestRecordVOS);
    }




}

