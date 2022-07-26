package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.enums.BorrowOrderStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;

import java.util.Date;
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

    IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query);

    BorrowCoinConfigVO config();

    void borrowCoin(BorrowOrderBO borrowCoinOrderDTO);

    BorrowCoinOrderVO info(Long orderId);

    BorrowRecordVO borrowRecord(Long orderId);

    IPage<BorrowPledgeRecordVO> pledgeRecord (PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query);

    IPage<BorrowInterestRecordVO> interestRecord(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query);

    IPage<BorrowRepayRecordVO> repayRecord(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query);

    void orderRepay(BorrowOrderRepayBO bo);

    void adjustPledge(AdjustPledgeBO bo);

    void forcedLiquidation(Long orderId);

    BorrowOrderStatisticsVO statistics(Date startTime,Date endTime);

    List<BorrowOrderStatisticsChartVO> statisticsChart(BorrowOrderStatisticsType statisticsType);

}