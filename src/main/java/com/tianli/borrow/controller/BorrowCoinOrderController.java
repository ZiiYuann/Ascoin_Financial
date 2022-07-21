package com.tianli.borrow.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.dto.BorrowCoinOrderDTO;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.query.BorrowCoinOrderQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import com.tianli.borrow.vo.BorrowCoinMainPageVO;
import com.tianli.borrow.vo.BorrowCoinOrderVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/history/order/list")
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
    @PostMapping("/borrow/coin")
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
        borrowCoinOrderService.info(orderId);
        return Result.success();
    }




}

