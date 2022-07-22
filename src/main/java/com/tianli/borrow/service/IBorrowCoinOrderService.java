package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.controller.BorrowCoinOrderController;
import com.tianli.borrow.dto.BorrowCoinOrderDTO;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.query.BorrowCoinOrderQuery;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;

import java.util.List;

/**
 * <p>
 * 借币订单 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
public interface IBorrowCoinOrderService extends IService<BorrowCoinOrder> {
    BorrowCoinMainPageVO mainPage();

    IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowCoinOrderQuery query);

    BorrowCoinConfigVO config();

    void borrowCoin(BorrowCoinOrderDTO borrowCoinOrderDTO);

    BorrowCoinOrderVO info(Long orderId);

    BorrowRecordVO borrowRecord(Long orderId);

    List<BorrowPledgeRecordVO> pledgeRecord (Long orderId);

    List<BorrowInterestRecordVO> interestRecord(Long orderId);

    List<BorrowRepayRecordVO> repayRecord(Long orderId);

}
